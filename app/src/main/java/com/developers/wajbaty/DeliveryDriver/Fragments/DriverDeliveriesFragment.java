package com.developers.wajbaty.DeliveryDriver.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.developers.wajbaty.Adapters.DriverDeliveriesAdapter;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverDeliveriesFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener{

    private static final String ADDRESS_MAP = "addressMap";

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10,
            MIN_UPDATE_DISTANCE = 10;

    private static final int RADIUS = 10 * 1000;

    private Map<String,Object> addressMap;
    private String currentUID;

    //views
    private SwitchMaterial driverDeliveriesWorkingSwitch;
    private RecyclerView driverDeliveriesRv;
    private View currentDeliveryLayout;

    //adapter
    private DriverDeliveriesAdapter deliveriesAdapter;
    private ArrayList<Delivery> deliveries;

    //delivery listener
    private List<ListenerRegistration> snapShotListeners;

    public DriverDeliveriesFragment() {
    }

    public static DriverDeliveriesFragment newInstance(Map<String, Object> addressMap) {
        DriverDeliveriesFragment fragment = new DriverDeliveriesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ADDRESS_MAP, (Serializable) addressMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            addressMap = (HashMap<String, Object>) getArguments().getSerializable(ADDRESS_MAP);
        }

        deliveries = new ArrayList<>();
        deliveriesAdapter = new DriverDeliveriesAdapter(deliveries);

        currentUID = FirebaseAuth.getInstance().getUid();


        snapShotListeners = new ArrayList<>();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_driver_delivries, container, false);

        driverDeliveriesWorkingSwitch = view.findViewById(R.id.driverDeliveriesWorkingSwitch);
        driverDeliveriesRv = view.findViewById(R.id.driverDeliveriesRv);
        currentDeliveryLayout = view.findViewById(R.id.currentDeliveryLayout);

        driverDeliveriesWorkingSwitch.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverDeliveriesRv.setAdapter(deliveriesAdapter);

//        listenToDeliveryRequests();

    }

    void listenToDeliveryRequests(){

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);


        final List<GeoQueryBounds> geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, RADIUS);

        final Query query =
                FirebaseFirestore.getInstance().collection("Deliveries")
                        .whereEqualTo("status",Delivery.STATUS_PENDING)
                        .orderBy("geohash")
                        .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                        .limit(5);

//        final List<Task<QuerySnapshot>> offerTasks = new ArrayList<>();


        for (GeoQueryBounds b : geoQueryBounds) {
            snapShotListeners.add(query.startAt(b.startHash).endAt(b.endHash).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(value!=null){

                        for(DocumentChange dc:value.getDocumentChanges()){

                            final DocumentSnapshot snapshot = dc.getDocument();
                            final String docID = snapshot.getId();
                            boolean alreadyExists = false;
                            int position = -1;

                            for(int i=0;i<deliveries.size();i++){
                                if(deliveries.get(i).getID().equals(docID)){
                                    position = i;
                                    alreadyExists = true;
                                    break;
                                }
                            }

                            if(alreadyExists){

                                if(snapshot.contains("status") &&
                                        snapshot.getLong("status") == Delivery.STATUS_ACCEPTED){

                                    if(snapshot.contains("driverID")){

                                        String driverID = snapshot.getString("driverID");

                                        if(driverID.equals(currentUID)){

                                            currentDeliveryLayout.setVisibility(View.VISIBLE);


                                        }else if(position != -1){

                                            deliveries.remove(position);
                                            deliveriesAdapter.notifyItemRemoved(position);

                                        }

                                    }

                                }

                            }else{

                                deliveries.add(snapshot.toObject(Delivery.class));
                                deliveriesAdapter.notifyItemInserted(deliveries.size());

                            }

                        }

                    }

                }
            }));
        }



    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }


    @Override
    public void onDestroy() {

        if(!snapShotListeners.isEmpty()){
            for(ListenerRegistration listenerRegistration:snapShotListeners){
                listenerRegistration.remove();
            }
        }

        super.onDestroy();
    }


}