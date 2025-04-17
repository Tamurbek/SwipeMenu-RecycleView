package com.foodapp.swipemenuapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<String> itemList;
    private Context context;
    private RecyclerView recyclerView;
    private MyViewHolder openedViewHolder = null;

    public MyAdapter(List<String> itemList, Context context, RecyclerView recyclerView) {
        this.itemList = itemList;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.itemText.setText(itemList.get(position));
//        holder.resetForeground();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        View viewForeground, btnEdit, btnOrder;
        float downX;
        boolean isSwiped = false;
        Handler handler = new Handler();
        Runnable autoCloseRunnable;

        int btnEditWidth = 0;
        int btnOrderWidth = 0;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnOrder = itemView.findViewById(R.id.btnOrder);

            itemView.post(() -> {
                btnEditWidth = btnEdit.getWidth();
                btnOrderWidth = btnOrder.getWidth();
            });

            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (!isSwiped && viewForeground.getTranslationX() == 0f) {
                        Toast.makeText(context, "Item clicked: " + itemText.getText(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                float currentX = event.getX();
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = currentX;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = currentX - downX;

                        // Maksimum surish chegaralari
                        float maxRight = btnEditWidth;
                        float maxLeft = -btnOrderWidth;

                        // Chegaralangan surish (real vaqtli)
                        float newTranslationX = Math.min(Math.max(deltaX, maxLeft), maxRight);
                        viewForeground.setTranslationX(newTranslationX);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float totalDeltaX = currentX - downX;
                        float swipeThreshold = dpToPx(50);

                        // Boshqa item ochiq bo‘lsa, yopamiz
                        if (openedViewHolder != null && openedViewHolder != this) {
                            openedViewHolder.closeSwipe();
                        }

                        if (totalDeltaX < -swipeThreshold) {
                            // Chapga to‘liq ochish
                            openSwipe(-btnOrderWidth);
                        } else if (totalDeltaX > swipeThreshold) {
                            // O‘ngga to‘liq ochish
                            openSwipe(btnEditWidth);
                        } else {
                            // Yetarli emas – bekor qilish
                            closeSwipe();
                        }
                        return true;
                }
                return false;
            });


//            viewForeground.setOnClickListener(v -> {
//                if (!isSwiped && viewForeground.getTranslationX() == 0f) {
//                    Toast.makeText(context, "Item clicked: " + itemText.getText(), Toast.LENGTH_SHORT).show();
//                }
//            });

            btnEdit.setOnClickListener(v -> {
                Toast.makeText(context, "Edit Clicked: " + itemText.getText(), Toast.LENGTH_SHORT).show();
                closeSwipe();
            });

            btnOrder.setOnClickListener(v -> {
                Toast.makeText(context, "Order Clicked: " + itemText.getText(), Toast.LENGTH_SHORT).show();
                closeSwipe();
            });
        }

        private void openSwipe(float distance) {
            viewForeground.animate().translationX(distance).setDuration(200).start();
            isSwiped = true;
            openedViewHolder = this;
//            startAutoCloseTimer();
        }

        private void startAutoCloseTimer() {
            if (autoCloseRunnable != null) {
                handler.removeCallbacks(autoCloseRunnable);
            }
            autoCloseRunnable = this::closeSwipe;
            handler.postDelayed(autoCloseRunnable, 1000); // 1 soniya
        }

        public void closeSwipe() {
            viewForeground.animate().translationX(0f).setDuration(200).start();
            isSwiped = false;
            if (openedViewHolder == this) {
                openedViewHolder = null;
            }
        }

        public void resetForeground() {
            viewForeground.setTranslationX(0f);
            isSwiped = false;
        }

        private float dpToPx(float dp) {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    context.getResources().getDisplayMetrics()
            );
        }
    }
}
