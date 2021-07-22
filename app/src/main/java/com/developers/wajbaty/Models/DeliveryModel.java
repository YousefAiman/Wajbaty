package com.developers.wajbaty.Models;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.developers.wajbaty.Models.offer.DiscountOffer;
import com.developers.wajbaty.Models.offer.Offer;
import com.developers.wajbaty.Utils.CloudMessagingNotificationsSender;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

public class DeliveryModel extends Observable {

    public static final int DELIVERY_REQUEST_SUCCESS = 1,DELIVERY_REQUEST_FAILED = 2,
        DELIVERY_DRIVER_NOT_FOUND = 3,
        DELIVERY_DRIVERS_NOTIFIED = 4,
        DELIVERY_DRIVER_ACCEPTED_DELIVERY = 5;

    private final Delivery delivery;
    private final DocumentReference deliveryRef;
    private final FirebaseFirestore firestore;

    public DeliveryModel(Delivery delivery){
        this.delivery = delivery;
        firestore = FirebaseFirestore.getInstance();
        deliveryRef = firestore.collection("Deliveries")
                .document(delivery.getID());
    }

    public void requestDelivery(HashMap<String,Object> locationMap,ArrayList<CartItem> cartItems){

        deliveryRef.set(delivery).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.d("ttt","delivery added");

                final CollectionReference deliveryCartRef = deliveryRef.collection("Deliveries");

                List<Task<?>> cartTasks = new ArrayList<>();

                for(CartItem cartItem:cartItems){
                    cartTasks.add(deliveryCartRef.add(cartItem));
                }

                Tasks.whenAllSuccess(cartTasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {

                        Log.d("ttt","");

                        notifyDeliveryDrivers(locationMap);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                notifyError(DELIVERY_REQUEST_FAILED,e.getMessage());

            }
        });

    }

    private void notifyDeliveryDrivers(HashMap<String,Object> locationMap){

        final GeoLocation center = new GeoLocation(
                delivery.getDeliveryLocation().getLatitude(),
                delivery.getDeliveryLocation().getLongitude());

        final List<GeoQueryBounds> geoQueryBounds =
                GeoFireUtils.getGeoHashQueryBounds(center, 100 * 1000);

        final Query driversQuery =
                firestore.collection("Users")
                        .whereEqualTo("countryCode", locationMap.get("countryCode"))
                        .orderBy("geohash");

        List<Task<QuerySnapshot>> driverTasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query query = driversQuery.startAt(b.startHash).endAt(b.endHash);
            driverTasks.add(query.get());
        }

        firestore.collection("Users").document(delivery.getRequesterID())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Log.d("ttt","gotten requester info");

                CloudMessagingNotificationsSender.Data data =
                        new CloudMessagingNotificationsSender.Data(
                                delivery.getRequesterID(),
                                "Delivery Request",
                                documentSnapshot.getString("name") + " ordered delivery",
                                documentSnapshot.getString("imageUrl"),
                                delivery.getID(),
                                CloudMessagingNotificationsSender.Data.TYPE_DELIVERY_REQUEST);


                Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        Log.d("ttt","all driver tasks complete");

                        boolean noResult = true;

                        for (Task<QuerySnapshot> driverTask : driverTasks) {

                            Log.d("ttt","driverTask.isSuccessful(): "+driverTask.isSuccessful());

                            if(driverTask.isSuccessful() && driverTask.getResult()!=null && !driverTask.getResult().isEmpty()){

                                Log.d("ttt","drivers found in task");

                                for(DocumentSnapshot snapshot:driverTask.getResult()){

                                    Log.d("ttt","driver found: "+snapshot.getId());

                                    CloudMessagingNotificationsSender.sendNotification(snapshot.getId(), data);
                                }

                                noResult = false;
                            }else if(!driverTask.isSuccessful()){

                                if(driverTask.getException()!=null &&
                                        driverTask.getException().getMessage()!=null){

                                    Log.d("ttt","driver error: "+driverTask.getException().getMessage());

                                }

                            }
                        }

                        setChanged();
                        if(noResult){
                            notifyError(DELIVERY_DRIVER_NOT_FOUND,"No drivers found in your range");
                        }else{
                            notifyObservers(DELIVERY_DRIVERS_NOTIFIED);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ttt","failed to get offers: "+e.getMessage());
                        notifyError(DELIVERY_DRIVER_NOT_FOUND,e.getMessage());

                    }
                });

            }
        });
//        Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//            @Override
//            public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                for(Task<QuerySnapshot> driverTask: driverTasks){
//
//                    if(driverTask.isSuccessful() && driverTask.getResult()!=null
//                    && !driverTask.getResult().isEmpty()){
//
//                        for(DocumentSnapshot snapshot:driverTask.getResult()){
//
//                            firestore.collection("Users").document(snapshot.getId())
//                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                                    CloudMessagingNotificationsSender.Data data =
//                                            new CloudMessagingNotificationsSender.Data(
//                                                    delivery.getRequesterID(),
//                                                    "Delivery Request",
//                                                    documentSnapshot.getString("name") + " ordered delivery",
//                                                    documentSnapshot.getString("imageUrl"),
//                                                    delivery.getID(),
//                                                    CloudMessagingNotificationsSender.Data.TYPE_DELIVERY_REQUEST);
//
//
//                                    Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                                            boolean allFailed = true;
//
//                                            for (Task<QuerySnapshot> driverTask : driverTasks) {
//                                                if(driverTask.isSuccessful() && driverTask.getResult()!=null && !driverTask.getResult().isEmpty()){
//
//                                                    for(DocumentSnapshot snapshot:driverTask.getResult()){
//                                                        CloudMessagingNotificationsSender.sendNotification(snapshot.getId(), data);
//                                                    }
//
//                                                    allFailed = false;
//                                                }
//                                            }
//
//                                            if(allFailed){
//                                                notifyError(DELIVERY_DRIVER_NOT_FOUND,"No drivers found in your range");
//                                            }else{
//                                                setChanged();
//                                                notifyObservers(DELIVERY_DRIVERS_NOTIFIED);
//
//                                            }
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d("ttt","failed to get offers: "+e.getMessage());
//                                            notifyError(DELIVERY_DRIVER_NOT_FOUND,e.getMessage());
//
//                                        }
//                                    });
//
//                                }
//                            });
//                        }
//
//                    }
//                }
//
//
//            }
//        });
//
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    }


    public ListenerRegistration listenForDriverDeliveryAcceptance(){

        return deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

               if(value!=null){

                   if(value.contains("status") && value.getLong("status") == Delivery.STATUS_ACCEPTED){

                       delivery.setStatus(Delivery.STATUS_ACCEPTED);

                       Delivery.InProgressDelivery inProgressDelivery =
                               (Delivery.InProgressDelivery) delivery;

                       inProgressDelivery.setDriverID(value.getString("driverID"));

                        setChanged();
                        notifyObservers(inProgressDelivery);

                   }

               }

            }
        });

    }

    private void notifyError(int key,String error){
        final HashMap<Integer,Object> resultMap = new HashMap<>();
        resultMap.put(key,error);
        setChanged();
        notifyObservers(resultMap);
    }

}
