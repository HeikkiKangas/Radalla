package xyz.dradge.radalla.tabs.station;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.dradge.radalla.MapActivity;
import xyz.dradge.radalla.MainActivity;
import xyz.dradge.radalla.R;
import xyz.dradge.radalla.model.TimeTableRow;
import xyz.dradge.radalla.model.Train;

/**
 * RecyclerView adapter for list items in station view.
 */
public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<Train> trains;

    /**
     * Constructor that sets the trains to show.
     * @param trains the trains to show.
     */
    public StationAdapter(List<Train> trains) {
        this.trains = trains;
    }

    /**
     * Inflates the ViewHolder layout for a single train on station view.
     * @param parent used for inflating the layout.
     * @param viewType not used.
     * @return the inflated ViewHolder.
     */
    @NonNull
    @Override
    public StationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.station_view_item, parent, false));
    }

    /**
     * Sets up the given ViewHolder with the data of
     * train at given position on the RecyclerView list.
     * @param holder ViewHolder for current train.
     * @param position position of the current train.
     */
    @Override
    public void onBindViewHolder(@NonNull StationAdapter.ViewHolder holder, int position) {
        Train t = trains.get(position);
        TimeTableRow departureRow = t.getDepartureRow();
        holder.departureTime.setText(departureRow == null
                ? ""
                : t.getOriginDepartureTime()
        );
        holder.trainName.setText(
                t.getCommuterLineID().isEmpty()
                ? t.getTrainType().replace("HDM", "H") + ' ' + t.getTrainNumber()
                : t.getCommuterLineID()
        );

        holder.arrivalTime.setText((t.getDestinationArrivalTime()));
        holder.destinationName.setText(t.getDestination().getStationFriendlyName());
        holder.trainLocationTopic = t.getLocationMQTTTopic();
        holder.runningCurrently = t.isRunningCurrently();
    }

    /**
     * Getter for count of trains on the RecyclerView.
     * @return count of trains on the RecyclerView.
     */
    @Override
    public int getItemCount() {
        return trains.size();
    }

    /**
     * Holder for Views representing the data of single train.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView departureTime;
        public TextView trainName;
        public TextView arrivalTime;
        public TextView destinationName;
        public String trainLocationTopic;
        public boolean runningCurrently;

        /**
         * Constructor that initializes Views needed and sets long click listener for list item.
         * @param v the ViewHolder.
         */
        public ViewHolder(View v) {
            super(v);
            departureTime = v.findViewById(R.id.stationItemDepartureTime);
            trainName = v.findViewById(R.id.stationItemTrainName);
            arrivalTime = v.findViewById(R.id.stationItemArrivalTime);
            destinationName = v.findViewById(R.id.stationItemDestinationName);
            v.setOnLongClickListener(this);
        }

        /**
         * Long click listener for list item, tracks train's location on map.
         * @param v the ViewHolder.
         * @return true.
         */
        @Override
        public boolean onLongClick(View v) {
            //Log.d(getClass().getName(), "onLongClick");
            if (runningCurrently) {
                Context c = v.getContext();
                Intent i = new Intent(c, MapActivity.class);
                i.putExtra("topic", trainLocationTopic);
                c.startActivity(i);
            } else {
                ((MainActivity) v.getContext()).makeToast(
                                trainName.getText().toString() + " is not running currently");
            }
            return true;
        }
    }
}
