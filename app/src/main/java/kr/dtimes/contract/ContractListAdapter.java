package kr.dtimes.contract;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ContractListAdapter extends BaseAdapter{
    private Context context;
    private List<Contract> contractList;

    public ContractListAdapter(Context context, List<Contract> contractList) {
        this.context = context;
        this.contractList = contractList;
    }

    @Override
    public int getCount() {
        return contractList.size();
    }

    @Override
    public Object getItem(int position) {
        return contractList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.contract, null);

        TextView contractTime = (TextView) v.findViewById(R.id.contractTime);
        TextView contractText = (TextView) v.findViewById(R.id.contractText);

        contractTime.setText(contractList.get(position).getContractTime());
        contractText.setText(contractList.get(position).getContractText());

        v.setTag(contractList.get(position).getContractTime());
        return v;
    }
}
