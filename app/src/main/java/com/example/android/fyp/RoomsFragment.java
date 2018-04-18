package com.example.android.fyp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucy on 05/04/2018.
 */

public class RoomsFragment extends Fragment implements View.OnClickListener {
    View view;
    Spinner roomSpinner;
    ArrayList<String> roomNames = new ArrayList<>();
    Button selectButton;
    TourFunctions thisTourFunctions;
    Map<Integer, String> thisRoomMap = new HashMap<>();

    public void passData(TourFunctions mTourFunctions, Map roomMap){
        thisTourFunctions = mTourFunctions;
        thisRoomMap = roomMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        for (Integer key: thisRoomMap.keySet()){
            roomNames.add(thisRoomMap.get(key));
        }

        view = inflater.inflate(R.layout.chooseroomfragment, container, false);
        roomSpinner = (Spinner) view.findViewById(R.id.fragRoomNameSpinner);
        selectButton = (Button) view.findViewById(R.id.fragChooseRoom);

        if (roomNames != null){
            ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), R.layout.spinner_item, roomNames);
            roomSpinner.setAdapter(adapter);
        }

        selectButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (roomSpinner.getSelectedItem() != null) {
            Integer keyValue = 0;
            for (int key : thisRoomMap.keySet()) {
                if (thisRoomMap.get(key).equals(roomSpinner.getSelectedItem())) {
                    keyValue = Integer.valueOf(key); //return the first found
                }
            }

            thisTourFunctions.setLocationOfflineMode(keyValue);
        }
        else {
            Toast.makeText(getActivity(), "Please Select a Room", Toast.LENGTH_LONG).show();
        }
    }

}




