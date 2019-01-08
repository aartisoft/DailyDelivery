package in.dailydelivery.dailydelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;

public class OrdersDisplayRecylcerViewAdapter extends RecyclerView.Adapter<OrdersDisplayRecylcerViewAdapter.ViewHolder> {
    List<OneTimeOrderDetails> items;
    DeleteOto deleteOto;

    public OrdersDisplayRecylcerViewAdapter(List<OneTimeOrderDetails> items, DeleteOto deleteOto) {
        this.items = items;
        this.deleteOto = deleteOto;
    }

    @NonNull
    @Override
    public OrdersDisplayRecylcerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_list, parent, false);
        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView desTV = view.findViewById(R.id.desTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView slotTV = view.findViewById(R.id.deliverySlotTV);
        TextView statusTV = view.findViewById(R.id.statusTV);
        ImageView delBtn = view.findViewById(R.id.delBtn);
        return new ViewHolder(view, nameTV, desTV, priceTV, slotTV, statusTV, delBtn);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrdersDisplayRecylcerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        int ddPrice = items.get(position).getPrice() * items.get(position).getQty();
        holder.nameTV.setText(items.get(position).getName() + "(" + items.get(position).getQty() + " Nos. )");
        holder.desTV.setText(items.get(position).getDes());
        holder.priceTV.setText("Rs. " + ddPrice);
        Log.d("DD", "Delivery Slot: " + items.get(position).getDeliverySlot());
        if (items.get(position).getDeliverySlot() == 1) {
            holder.slotTV.setText("Delivery: 5:30AM to 7:30AM");
        } else if (items.get(position).getDeliverySlot() == 2) {
            holder.slotTV.setText("Delivery: 6 PM to 8 PM");
        }
        String status;
        switch (holder.mItem.getStatus()) {
            case 1:
                status = "Scheduled";
                holder.statusTV.setTextColor(Color.parseColor("#732525"));
                break;
            case 2:
                status = "Cancelled";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            case 3:
                status = "Confirmed";
                holder.statusTV.setTextColor(Color.parseColor("#33862e"));
                break;
            case 4:
                status = "Delivered";
                holder.statusTV.setTextColor(Color.parseColor("#33862e"));
                break;
            case 5:
                status = "Un delivered";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            default:
                status = "NA";
                break;
        }
        status = "Status: " + status;
        holder.statusTV.setText(status);
        if (holder.mItem.getStatus() == 2 || holder.mItem.getStatus() == 4 || holder.mItem.getStatus() == 5) {
            holder.delBtn.setVisibility(View.GONE);
            //holder.statusTV.setTextColor(Color.parseColor("#12345F"));
        } else {
            holder.delBtn.setVisibility(View.VISIBLE);
        }
        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the order at server
                deleteOto.deleteOto(holder.mItem.getOrderId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public final TextView nameTV;
        public final TextView desTV;
        public final TextView priceTV;
        public final TextView slotTV;
        public final TextView statusTV;
        public final ImageView delBtn;

        public ViewHolder(View mView, TextView nameTV, TextView desTV, TextView priceTV, TextView slotTV, TextView statusTV, ImageView delBtn) {
            super(mView);
            this.mView = mView;
            this.nameTV = nameTV;
            this.desTV = desTV;
            this.priceTV = priceTV;
            this.slotTV = slotTV;
            this.statusTV = statusTV;
            this.delBtn = delBtn;
        }

        public OneTimeOrderDetails mItem;
    }

    public void updateData(List<OneTimeOrderDetails> list) {
        items.clear();
        items = list;

        notifyDataSetChanged();
    }

    public interface DeleteOto {
        void deleteOto(int otoId);
    }
}


