package com.matvey.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String PREFS = "clicker";

    private long coins;
    private long perClick;
    private long perSecond;

    private TextView coinsText;
    private TextView statsText;
    private Button clickUpgradeButton;
    private Button autoUpgradeButton;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoTick = new Runnable() {
        @Override
        public void run() {
            if (perSecond > 0) {
                coins += perSecond;
                updateUi();
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        coins = prefs.getLong("coins", 0);
        perClick = prefs.getLong("perClick", 1);
        perSecond = prefs.getLong("perSecond", 0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.parseColor("#1E1B2E"));
        int pad = dp(24);
        root.setPadding(pad, dp(48), pad, pad);

        coinsText = new TextView(this);
        coinsText.setTextColor(Color.WHITE);
        coinsText.setTextSize(44);
        coinsText.setGravity(Gravity.CENTER);
        root.addView(coinsText);

        statsText = new TextView(this);
        statsText.setTextColor(Color.parseColor("#B0A8D0"));
        statsText.setTextSize(16);
        statsText.setGravity(Gravity.CENTER);
        root.addView(statsText);

        Button clickButton = new Button(this);
        clickButton.setText("ЖМИ!");
        clickButton.setTextSize(32);
        clickButton.setTextColor(Color.WHITE);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor("#7C4DFF"));
        clickButton.setBackground(circle);
        int size = dp(220);
        LinearLayout.LayoutParams clickParams = new LinearLayout.LayoutParams(size, size);
        clickParams.topMargin = dp(40);
        clickParams.bottomMargin = dp(40);
        root.addView(clickButton, clickParams);

        clickButton.setOnClickListener(v -> {
            coins += perClick;
            updateUi();
            v.animate().cancel();
            v.setScaleX(0.9f);
            v.setScaleY(0.9f);
            v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
        });

        clickUpgradeButton = makeShopButton();
        root.addView(clickUpgradeButton, shopParams());
        clickUpgradeButton.setOnClickListener(v -> {
            long cost = clickUpgradeCost();
            if (coins >= cost) {
                coins -= cost;
                perClick++;
                updateUi();
            }
        });

        autoUpgradeButton = makeShopButton();
        root.addView(autoUpgradeButton, shopParams());
        autoUpgradeButton.setOnClickListener(v -> {
            long cost = autoUpgradeCost();
            if (coins >= cost) {
                coins -= cost;
                perSecond++;
                updateUi();
            }
        });

        Button resetButton = new Button(this);
        resetButton.setText("Сбросить");
        resetButton.setTextColor(Color.parseColor("#B0A8D0"));
        resetButton.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams resetParams = shopParams();
        resetParams.topMargin = dp(24);
        root.addView(resetButton, resetParams);
        resetButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Сбросить прогресс?")
                .setMessage("Все монеты и улучшения пропадут.")
                .setPositiveButton("Да", (d, w) -> {
                    coins = 0;
                    perClick = 1;
                    perSecond = 0;
                    updateUi();
                })
                .setNegativeButton("Нет", null)
                .show());

        setContentView(root);
        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(autoTick, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoTick);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putLong("coins", coins)
                .putLong("perClick", perClick)
                .putLong("perSecond", perSecond)
                .apply();
    }

    private long clickUpgradeCost() {
        return 10 * perClick * perClick;
    }

    private long autoUpgradeCost() {
        return 50 * (perSecond + 1) * (perSecond + 1);
    }

    private void updateUi() {
        coinsText.setText(coins + " 💰");
        statsText.setText("+" + perClick + " за клик · +" + perSecond + " в секунду");

        long clickCost = clickUpgradeCost();
        clickUpgradeButton.setText("+1 за клик — " + clickCost + " 💰");
        clickUpgradeButton.setEnabled(coins >= clickCost);
        clickUpgradeButton.setAlpha(coins >= clickCost ? 1f : 0.5f);

        long autoCost = autoUpgradeCost();
        autoUpgradeButton.setText("Автокликер +1/сек — " + autoCost + " 💰");
        autoUpgradeButton.setEnabled(coins >= autoCost);
        autoUpgradeButton.setAlpha(coins >= autoCost ? 1f : 0.5f);
    }

    private Button makeShopButton() {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#2E2A45"));
        b.setBackground(bg);
        return b;
    }

    private LinearLayout.LayoutParams shopParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        p.topMargin = dp(8);
        return p;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
