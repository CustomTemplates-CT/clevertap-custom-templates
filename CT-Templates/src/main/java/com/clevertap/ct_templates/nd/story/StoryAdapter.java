package com.clevertap.ct_templates.nd.story;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.clevertap.ct_templates.R;
import com.clevertap.ct_templates.common.Utils;
import com.clevertap.ct_templates.databinding.ItemStoryBinding;
import com.clevertap.ct_templates.nd.NativeDisplayListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    Activity context;
    JSONArray jsonArray;
    NativeDisplayListener listener;

    public StoryAdapter(Activity context, JSONArray jsonArray, NativeDisplayListener listener, Boolean shouldAppend) {
        this.context = context;
        this.jsonArray = jsonArray;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(
                LayoutInflater.from(context)
        );
        return new StoryViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        try {
            holder.bind(jsonArray.getJSONObject(position), position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {

        ItemStoryBinding binding;
        NativeDisplayListener listener;

        public StoryViewHolder(ItemStoryBinding binding, NativeDisplayListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(JSONObject unit, int position) throws JSONException {
            Utils.loadImageURLIntoImageView(binding.imageStory, unit.getJSONObject("media").getString("url"), context);
            binding.tvName.setText(unit.getJSONObject("title").getString("text"));
            if (unit.getBoolean("clicked")) {
                binding.imageStory.setBorderColor(context.getResources().getColor(R.color.light_gray));
            } else {
                binding.imageStory.setBorderColor(context.getResources().getColor(R.color.colorAccent));
            }
            binding.getRoot().setOnClickListener(view -> {
                try {
                    unit.put("clicked", true);
                    listener.onClick(binding.imageStory.getId(), "nd_stories", unit.getString("dl"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
