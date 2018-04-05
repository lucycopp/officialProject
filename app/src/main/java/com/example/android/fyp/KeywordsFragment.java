package com.example.android.fyp;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.ls.LSException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucy on 05/04/2018.
 */

public class KeywordsFragment extends Fragment {
    View view;
    ListView keywordsList;
    ArrayList<String> keywords;
    Button firstButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        keywords =  getArguments().getStringArrayList("KeywordList");
        view = inflater.inflate(R.layout.keywords_fragment, container, false);
        keywordsList = (ListView) view.findViewById(R.id.keywordsListViewFragment);

        if (keywords != null){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, keywords);
            keywordsList.setAdapter(adapter);
        } else{
            ArrayList<String> list = new ArrayList<>();
            list.add("No keywords found.");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
            keywordsList.setAdapter(adapter);
        }



        return view;
    }
}


