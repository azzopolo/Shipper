package com.icanstudioz.taxi.adapter;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.fragement.PaymentDetail;
import com.icanstudioz.taxi.fragement.PaymentHistory;
import com.icanstudioz.taxi.pojo.PendingRequestPojo;

import java.util.List;


/**
 * Created by android on 8/3/17.
 */

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.Holder> {
    List<PendingRequestPojo> list;

    public PaymentAdapter(List<PendingRequestPojo> list) {
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final PendingRequestPojo pojo = list.get(position);
        holder.from_add.setText(pojo.getPickup_adress());
        holder.to_add.setText(pojo.getDrop_address());
        holder.status.setText(pojo.getPayment_status());
        holder.time.setText(Utils.getformattedTime(pojo.getTime()));
        Utils utils = new Utils();
        holder.date.setText(utils.getCurrentDateInSpecificFormat(pojo.getTime()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", pojo);
               /* bundle.putString("pickupaddress", list.get(holder.getAdapterPosition()).getPickup_adress());
                bundle.putString("dropaddress", list.get(holder.getAdapterPosition()).getDrop_address());

                bundle.putString("amount", list.get(holder.getAdapterPosition()).getAmount());

                bundle.putString("distance", list.get(holder.getAdapterPosition()).getDistance());
                bundle.putString("status", list.get(holder.getAdapterPosition()).getStatus());
                bundle.putString("payment_status", list.get(holder.getAdapterPosition()).getPayment_status());
                bundle.putString("time", list.get(holder.getAdapterPosition()).getTime());
*/
                PaymentDetail detailFragment = new PaymentDetail();
                detailFragment.setArguments(bundle);
                ((HomeActivity) holder.itemView.getContext()).changeFragment(detailFragment, "Payment Detail");
            }
        });
        BookFont(holder, holder.f);
        BookFont(holder, holder.t);
        BookFont(holder, holder.dn);
        BookFont(holder, holder.dt);

        MediumFont(holder, holder.from_add);
        MediumFont(holder, holder.to_add);
        MediumFont(holder, holder.date);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {


        TextView from, to, status, from_add, to_add, date, time;
        TextView f, t, dn, dt;

        public Holder(View itemView) {
            super(itemView);

            f = (TextView) itemView.findViewById(R.id.from);
            t = (TextView) itemView.findViewById(R.id.to);

            dn = (TextView) itemView.findViewById(R.id.status);
            dt = (TextView) itemView.findViewById(R.id.datee);


            status = (TextView) itemView.findViewById(R.id.txt_status);
            from_add = (TextView) itemView.findViewById(R.id.txt_from_add);
            to_add = (TextView) itemView.findViewById(R.id.txt_to_add);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
        }
    }

    public void BookFont(Holder holder, TextView view1) {
        Typeface font1 = Typeface.createFromAsset(holder.itemView.getContext().getAssets(), "font/AvenirLTStd_Book.otf");
        view1.setTypeface(font1);
    }

    public void MediumFont(Holder holder, TextView view) {
        Typeface font = Typeface.createFromAsset(holder.itemView.getContext().getAssets(), "font/AvenirLTStd_Medium.otf");
        view.setTypeface(font);
    }

}
