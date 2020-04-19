package xyz.dradge.radalla.tabs.station;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.dradge.radalla.R;
import xyz.dradge.radalla.model.TimeTableRow;
import xyz.dradge.radalla.model.Train;
import xyz.dradge.radalla.util.TimeUtil;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<Train> trains;

    public StationAdapter(List<Train> trains) {
        this.trains = trains;
    }

    @NonNull
    @Override
    public StationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.station_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StationAdapter.ViewHolder holder, int position) {
        Train t = trains.get(position);
        TimeTableRow departureRow = t.getDepartureRow();
        holder.departureTime.setText(departureRow == null
                ? "     "
                : TimeUtil.utcToHoursAndMinutes(departureRow.getScheduledTime())
        );
        holder.trainName.setText(
                t.getCommuterLineID().isEmpty()
                ? t.getTrainType().replace("HDM", "H") + ' ' + t.getTrainNumber()
                : t.getCommuterLineID()
        );
        holder.arrivalTime.setText(TimeUtil.utcToHoursAndMinutes(t.getArrivalRow().getScheduledTime()));
        holder.destinationName.setText(t.getDestination().getStationFriendlyName());
    }

    @Override
    public int getItemCount() {
        return trains.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView departureTime;
        public TextView trainName;
        public TextView arrivalTime;
        public TextView destinationName;
        public ViewHolder(View v) {
            super(v);
            departureTime = v.findViewById(R.id.stationItemDepartureTime);
            trainName = v.findViewById(R.id.stationItemTrainName);
            arrivalTime = v.findViewById(R.id.stationItemArrivalTime);
            destinationName = v.findViewById(R.id.stationItemDestinationName);
        }
    }
}
