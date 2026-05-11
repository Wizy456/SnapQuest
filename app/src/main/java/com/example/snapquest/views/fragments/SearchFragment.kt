package com.example.snapquest.views.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapquest.R
import com.example.snapquest.views.UserSearchAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController

class SearchFragment : Fragment() {

    private var etSearch: EditText? = null
    private var rvResults: RecyclerView? = null
    private var ivClear: ImageView? = null
    private var tvCancel: TextView? = null
    private var tvEmpty: TextView? = null
    private lateinit var adapter: UserSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, null)

        etSearch  = view.findViewById(R.id.etSearch)
        rvResults = view.findViewById(R.id.rvResults)
        ivClear   = view.findViewById(R.id.ivClear)
        tvCancel  = view.findViewById(R.id.tvCancel)
        tvEmpty   = view.findViewById(R.id.tvEmpty)

        adapter = UserSearchAdapter { userId ->
            val intent = Intent(requireContext(), OtherUserProfileFragment::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        rvResults?.layoutManager = LinearLayoutManager(requireContext())
        rvResults?.adapter = adapter

        // clear button
        ivClear?.setOnClickListener {
            etSearch?.setText("")
            adapter.submitList(emptyList())
            tvEmpty?.visibility = View.GONE
            ivClear?.visibility = View.GONE
            tvCancel?.visibility = View.GONE
        }

        // cancel button
        tvCancel?.setOnClickListener {
            etSearch?.setText("")
            adapter.submitList(emptyList())
            tvEmpty?.visibility = View.GONE
            ivClear?.visibility = View.GONE
            tvCancel?.visibility = View.GONE
            etSearch?.clearFocus()
        }

        // search listener
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                if (query.isEmpty()) {
                    adapter.submitList(emptyList())
                    tvEmpty?.visibility = View.GONE
                    ivClear?.visibility = View.GONE
                    tvCancel?.visibility = View.GONE
                    return
                }

                ivClear?.visibility = View.VISIBLE
                tvCancel?.visibility = View.VISIBLE

                FirebaseFirestore.getInstance()
                    .collection("accounts")
                    .get()
                    .addOnSuccessListener { snapshots ->

                        // filter using Kotlin contains AFTER fetching
                        val filtered = snapshots.documents.filter { doc ->
                            val name = doc.getString("name") ?: ""
                            name.contains(query, ignoreCase = true)
                        }
                        adapter.submitList(filtered)
                        tvEmpty?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    }
            }
        })
        etSearch?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearch?.windowToken, 0)
                true
            } else {
                false
            }
        }
        return view
    }
}