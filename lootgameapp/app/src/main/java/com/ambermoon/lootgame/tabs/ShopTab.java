package com.ambermoon.lootgame.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.graphics.CoinIconHelper;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopTab extends ScrollView {
    private LinearLayout itemGrid;
    private TextView balanceText;
    private List<SaveData.ShopItem> shopItems;
    private List<SaveData.PlayerListing> marketplaceListings;

    public ShopTab(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 16, 24, 16);

        // Header
        TextView title = new TextView(context);
        title.setText("SHOP");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Spend your coins on items");
        subtitle.setTextColor(Color.parseColor("#AAAACC"));
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 8);
        content.addView(subtitle);

        // Balance display
        balanceText = new TextView(context);
        balanceText.setTextColor(Color.parseColor("#FFD700"));
        balanceText.setTextSize(16);
        balanceText.setGravity(Gravity.CENTER);
        balanceText.setPadding(0, 8, 0, 16);
        content.addView(balanceText);

        // Item grid
        itemGrid = new LinearLayout(context);
        itemGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(itemGrid);

        addView(content);

        // Show loading text, then sync shop + marketplace + profile pics from cloud
        showLoading();
        SaveManager sm = SaveManager.getInstance();
        sm.syncShopFromCloud(items -> {
            shopItems = items;
            // Also sync marketplace listings
            sm.syncMarketplaceFromCloud((listings, coinsCollected) -> {
                marketplaceListings = listings;
                if (coinsCollected > 0) {
                    Toast.makeText(getContext(),
                            CoinIconHelper.withCoin(getContext(),
                            "Collected \u25C8 " + coinsCollected + " coins from sales!", 14),
                            Toast.LENGTH_LONG).show();
                    if (getContext() instanceof TabActivity) {
                        ((TabActivity) getContext()).updateCoinDisplay();
                    }
                }
                // Sync profile pics for seller display, then populate
                sm.syncProfilePicsFromCloud(pics -> populateGrid());
            });
        });
    }

    private void showLoading() {
        updateBalance();
        itemGrid.removeAllViews();
        TextView loading = new TextView(getContext());
        loading.setText("Loading shop...");
        loading.setTextColor(Color.parseColor("#AAAACC"));
        loading.setTextSize(14);
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0, 48, 0, 0);
        itemGrid.addView(loading);
    }

    private void refreshShop() {
        updateBalance();
        shopItems = SaveManager.getInstance().loadShopItems();
        marketplaceListings = SaveManager.getInstance().loadMarketplaceListings();
        populateGrid();
    }

    private void populateGrid() {
        updateBalance();
        itemGrid.removeAllViews();

        boolean hasShopItems = shopItems != null && !shopItems.isEmpty();
        boolean hasMarketplace = marketplaceListings != null && !marketplaceListings.isEmpty();

        if (!hasShopItems && !hasMarketplace) {
            TextView empty = new TextView(getContext());
            empty.setText("The shop is empty.\nCheck back later!");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 48, 0, 0);
            itemGrid.addView(empty);
            return;
        }

        // --- Lars's Shop Items ---
        if (hasShopItems) {
            TextView shopHeader = new TextView(getContext());
            shopHeader.setText("SHOP ITEMS");
            shopHeader.setTextColor(Color.parseColor("#FFD700"));
            shopHeader.setTextSize(15);
            shopHeader.setTypeface(Typeface.DEFAULT_BOLD);
            shopHeader.setGravity(Gravity.CENTER);
            shopHeader.setPadding(0, 4, 0, 8);
            itemGrid.addView(shopHeader);

            List<SaveData.ShopItem> sorted = new ArrayList<>(shopItems);
            Collections.sort(sorted, (a, b) -> a.price - b.price);

            for (SaveData.ShopItem si : sorted) {
                Item template = ItemRegistry.getTemplate(si.itemId);
                if (template == null) continue;
                addShopRow(si, template);
            }
        }

        // --- Player Marketplace Listings ---
        if (hasMarketplace) {
            // Spacer between sections
            if (hasShopItems) {
                View spacer = new View(getContext());
                spacer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 16));
                itemGrid.addView(spacer);
            }

            TextView marketHeader = new TextView(getContext());
            marketHeader.setText("PLAYER MARKET");
            marketHeader.setTextColor(Color.parseColor("#44BBFF"));
            marketHeader.setTextSize(15);
            marketHeader.setTypeface(Typeface.DEFAULT_BOLD);
            marketHeader.setGravity(Gravity.CENTER);
            marketHeader.setPadding(0, 4, 0, 4);
            itemGrid.addView(marketHeader);

            TextView marketSubtitle = new TextView(getContext());
            marketSubtitle.setText("Items listed by other players");
            marketSubtitle.setTextColor(Color.parseColor("#AAAACC"));
            marketSubtitle.setTextSize(11);
            marketSubtitle.setGravity(Gravity.CENTER);
            marketSubtitle.setPadding(0, 0, 0, 8);
            itemGrid.addView(marketSubtitle);

            List<SaveData.PlayerListing> sorted = new ArrayList<>(marketplaceListings);
            Collections.sort(sorted, (a, b) -> a.price - b.price);

            String currentUser = SaveManager.getInstance().getCurrentUsername();

            for (SaveData.PlayerListing pl : sorted) {
                Item template = ItemRegistry.getTemplate(pl.itemId);
                if (template == null) continue;
                boolean isOwnListing = pl.sellerUsername.equalsIgnoreCase(currentUser);
                addMarketplaceRow(pl, template, isOwnListing);
            }
        }
    }

    private void addShopRow(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#28233A"));
        row.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        // Item icon
        View iconView = new ItemIconView(ctx, template);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconView.setLayoutParams(iconParams);
        row.addView(iconView);

        // Item name + rarity
        LinearLayout nameCol = new LinearLayout(ctx);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(16, 0, 8, 0);
        LinearLayout.LayoutParams nameColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nameCol.setLayoutParams(nameColParams);

        TextView nameText = new TextView(ctx);
        nameText.setText(template.getName());
        nameText.setTextColor(rarityColor);
        nameText.setTextSize(14);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nameText);

        TextView rarityText = new TextView(ctx);
        rarityText.setText(template.getRarity().getDisplayName() + " " + template.getCategory().name());
        rarityText.setTextColor(Color.parseColor("#888899"));
        rarityText.setTextSize(10);
        nameCol.addView(rarityText);

        row.addView(nameCol);

        // Price + buy button
        LinearLayout buyCol = new LinearLayout(ctx);
        buyCol.setOrientation(LinearLayout.VERTICAL);
        buyCol.setGravity(Gravity.CENTER);

        TextView priceText = new TextView(ctx);
        CoinIconHelper.setCoinText(priceText,
                "\u25C8 " + shopItem.price, 14);
        priceText.setTextColor(Color.parseColor("#FFD700"));
        priceText.setTextSize(14);
        priceText.setGravity(Gravity.CENTER);
        buyCol.addView(priceText);

        Button buyBtn = new Button(ctx);
        buyBtn.setText("BUY");
        buyBtn.setTextColor(Color.WHITE);
        buyBtn.setTextSize(11);
        buyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        boolean canAfford = SaveManager.getInstance().getData().coins >= shopItem.price;
        buyBtn.setBackgroundColor(canAfford ? Color.parseColor("#2E8B57") : Color.parseColor("#444444"));
        buyBtn.setPadding(24, 4, 24, 4);
        buyBtn.setOnClickListener(v -> showBuyConfirmation(shopItem, template));
        buyCol.addView(buyBtn);

        row.addView(buyCol);

        // Tap row to see item detail
        row.setOnClickListener(v -> showItemDetail(shopItem, template));

        itemGrid.addView(row);
    }

    /**
     * Add a row for a player marketplace listing.
     */
    private void addMarketplaceRow(SaveData.PlayerListing listing, Item template, boolean isOwnListing) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#28233A"));
        row.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        // Item icon
        View iconView = new ItemIconView(ctx, template);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconView.setLayoutParams(iconParams);
        row.addView(iconView);

        // Item name + seller info
        LinearLayout nameCol = new LinearLayout(ctx);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(16, 0, 8, 0);
        LinearLayout.LayoutParams nameColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nameCol.setLayoutParams(nameColParams);

        TextView nameText = new TextView(ctx);
        nameText.setText(template.getName());
        nameText.setTextColor(rarityColor);
        nameText.setTextSize(14);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nameText);

        // Seller row: profile pic + username
        LinearLayout sellerRow = new LinearLayout(ctx);
        sellerRow.setOrientation(LinearLayout.HORIZONTAL);
        sellerRow.setGravity(Gravity.CENTER_VERTICAL);
        sellerRow.setPadding(0, 2, 0, 0);

        View sellerPicView = createSellerPicView(ctx, listing);
        sellerRow.addView(sellerPicView);

        TextView sellerText = new TextView(ctx);
        sellerText.setText(isOwnListing ? "YOUR LISTING" : listing.sellerUsername);
        sellerText.setTextColor(isOwnListing ? Color.parseColor("#44BBFF") : Color.parseColor("#888899"));
        sellerText.setTextSize(10);
        sellerText.setPadding(4, 0, 0, 0);
        sellerRow.addView(sellerText);

        nameCol.addView(sellerRow);
        row.addView(nameCol);

        // Price + buy/own indicator
        LinearLayout buyCol = new LinearLayout(ctx);
        buyCol.setOrientation(LinearLayout.VERTICAL);
        buyCol.setGravity(Gravity.CENTER);

        TextView priceText = new TextView(ctx);
        CoinIconHelper.setCoinText(priceText,
                "\u25C8 " + listing.price, 14);
        priceText.setTextColor(Color.parseColor("#FFD700"));
        priceText.setTextSize(14);
        priceText.setGravity(Gravity.CENTER);
        buyCol.addView(priceText);

        if (isOwnListing) {
            TextView ownLabel = new TextView(ctx);
            ownLabel.setText("YOURS");
            ownLabel.setTextColor(Color.parseColor("#44BBFF"));
            ownLabel.setTextSize(10);
            ownLabel.setGravity(Gravity.CENTER);
            ownLabel.setPadding(8, 4, 8, 4);
            buyCol.addView(ownLabel);
        } else {
            Button buyBtn = new Button(ctx);
            buyBtn.setText("BUY");
            buyBtn.setTextColor(Color.WHITE);
            buyBtn.setTextSize(11);
            buyBtn.setTypeface(Typeface.DEFAULT_BOLD);
            boolean canAfford = SaveManager.getInstance().getData().coins >= listing.price;
            buyBtn.setBackgroundColor(canAfford ? Color.parseColor("#2E8B57") : Color.parseColor("#444444"));
            buyBtn.setPadding(24, 4, 24, 4);
            buyBtn.setOnClickListener(v -> showMarketplaceBuyConfirmation(listing, template));
            buyCol.addView(buyBtn);
        }

        row.addView(buyCol);

        // Tap row to see item detail
        row.setOnClickListener(v -> showMarketplaceItemDetail(listing, template, isOwnListing));

        itemGrid.addView(row);
    }

    /**
     * Creates a circular profile picture (40x40) for a marketplace seller.
     * Shows the decoded Base64 image or a colored initial.
     * Profile pics are loaded from the shared cloud profile pics cache.
     */
    private View createSellerPicView(Context ctx, SaveData.PlayerListing listing) {
        String raw = SaveManager.getInstance().getProfilePic(listing.sellerUsername);
        if (raw == null) raw = "";
        final String initial = (listing.sellerUsername != null && !listing.sellerUsername.isEmpty())
                ? listing.sellerUsername.substring(0, 1).toUpperCase() : "?";

        Bitmap decoded = null;
        if (!raw.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(raw, Base64.NO_WRAP);
                decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception ignored) {}
        }
        final Bitmap picBitmap = decoded;

        View view = new SellerPicView(ctx, picBitmap, initial);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
        view.setLayoutParams(params);
        return view;
    }

    /**
     * Named View subclass for seller profile pictures.
     * Avoids D8 compiler bug with anonymous inner classes that have instance initializer blocks.
     */
    private static class SellerPicView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Bitmap picBitmap;
        private final String initial;

        SellerPicView(Context ctx, Bitmap picBitmap, String initial) {
            super(ctx);
            this.picBitmap = picBitmap;
            this.initial = initial;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy) - 1;

            if (picBitmap != null && !picBitmap.isRecycled()) {
                canvas.save();
                Path clipPath = new Path();
                clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
                canvas.clipPath(clipPath);
                RectF dst = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
                paint.setFilterBitmap(true);
                canvas.drawBitmap(picBitmap, null, dst, paint);
                canvas.restore();
            } else {
                paint.setColor(Color.parseColor("#3C3555"));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, radius, paint);
                paint.setColor(Color.parseColor("#B8A9D4"));
                paint.setTextSize(radius * 0.9f);
                paint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fm = paint.getFontMetrics();
                float textY = cy - (fm.ascent + fm.descent) / 2;
                canvas.drawText(initial, cx, textY, paint);
            }
        }
    }

    private void showItemDetail(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        // Item name
        TextView nameView = new TextView(ctx);
        nameView.setText(template.getName());
        nameView.setTextColor(rarityColor);
        nameView.setTextSize(18);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.bottomMargin = 16;
        nameView.setLayoutParams(nameParams);
        dialogContent.addView(nameView);

        // Animated item preview
        AnimatedItemView animatedView = new AnimatedItemView(ctx, rarityColor);
        LinearLayout.LayoutParams animParams = new LinearLayout.LayoutParams(160, 160);
        animParams.gravity = Gravity.CENTER;
        animParams.bottomMargin = 12;
        animatedView.setLayoutParams(animParams);
        dialogContent.addView(animatedView);

        // Start with idle animation
        String folderPath = template.getAnimationFolderPath();
        if (folderPath != null) {
            String idlePath = folderPath + "/idle.gif";
            if (AssetLoader.exists(idlePath)) {
                animatedView.playAnimation(idlePath);
            } else if (template.getTexturePath() != null) {
                animatedView.playAnimation(template.getTexturePath());
            }
        } else if (template.getTexturePath() != null) {
            animatedView.playAnimation(template.getTexturePath());
        }

        // Tooltip info
        TextView tooltipText = new TextView(ctx);
        tooltipText.setTextColor(Color.parseColor("#CCCCCC"));
        tooltipText.setTextSize(13);
        tooltipText.setGravity(Gravity.CENTER);
        tooltipText.setText(template.getTooltip());
        LinearLayout.LayoutParams tipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipParams.topMargin = 8;
        tooltipText.setLayoutParams(tipParams);
        dialogContent.addView(tooltipText);

        // Price
        TextView priceView = new TextView(ctx);
        CoinIconHelper.setCoinText(priceView,
                "\nPrice: \u25C8 " + shopItem.price + " coins", 16);
        priceView.setTextColor(Color.parseColor("#FFD700"));
        priceView.setTextSize(16);
        priceView.setGravity(Gravity.CENTER);
        priceView.setTypeface(Typeface.DEFAULT_BOLD);
        dialogContent.addView(priceView);

        // Currently owned count
        int owned = SaveManager.getInstance().getVaultItemCount(shopItem.itemId);
        if (owned > 0) {
            TextView ownedView = new TextView(ctx);
            ownedView.setText("You own: x" + owned);
            ownedView.setTextColor(Color.parseColor("#AAAACC"));
            ownedView.setTextSize(12);
            ownedView.setGravity(Gravity.CENTER);
            dialogContent.addView(ownedView);
        }

        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        boolean canAfford = SaveManager.getInstance().getData().coins >= shopItem.price;

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(dialogScroll);
        if (canAfford) {
            builder.setPositiveButton(CoinIconHelper.withCoin(ctx,
                    "Buy (\u25C8 " + shopItem.price + ")", 14), (dialog, which) -> {
                purchaseItem(shopItem, template);
            });
        }
        builder.setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    /**
     * Show detail dialog for a marketplace listing.
     */
    private void showMarketplaceItemDetail(SaveData.PlayerListing listing, Item template, boolean isOwnListing) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        // Item name
        TextView nameView = new TextView(ctx);
        nameView.setText(template.getName());
        nameView.setTextColor(rarityColor);
        nameView.setTextSize(18);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.bottomMargin = 16;
        nameView.setLayoutParams(nameParams);
        dialogContent.addView(nameView);

        // Animated item preview
        AnimatedItemView animatedView = new AnimatedItemView(ctx, rarityColor);
        LinearLayout.LayoutParams animParams = new LinearLayout.LayoutParams(160, 160);
        animParams.gravity = Gravity.CENTER;
        animParams.bottomMargin = 12;
        animatedView.setLayoutParams(animParams);
        dialogContent.addView(animatedView);

        String folderPath = template.getAnimationFolderPath();
        if (folderPath != null) {
            String idlePath = folderPath + "/idle.gif";
            if (AssetLoader.exists(idlePath)) {
                animatedView.playAnimation(idlePath);
            } else if (template.getTexturePath() != null) {
                animatedView.playAnimation(template.getTexturePath());
            }
        } else if (template.getTexturePath() != null) {
            animatedView.playAnimation(template.getTexturePath());
        }

        // Tooltip info
        TextView tooltipText = new TextView(ctx);
        tooltipText.setTextColor(Color.parseColor("#CCCCCC"));
        tooltipText.setTextSize(13);
        tooltipText.setGravity(Gravity.CENTER);
        tooltipText.setText(template.getTooltip());
        LinearLayout.LayoutParams tipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipParams.topMargin = 8;
        tooltipText.setLayoutParams(tipParams);
        dialogContent.addView(tooltipText);

        // Price
        TextView priceView = new TextView(ctx);
        CoinIconHelper.setCoinText(priceView,
                "\nPrice: \u25C8 " + listing.price + " coins", 16);
        priceView.setTextColor(Color.parseColor("#FFD700"));
        priceView.setTextSize(16);
        priceView.setGravity(Gravity.CENTER);
        priceView.setTypeface(Typeface.DEFAULT_BOLD);
        dialogContent.addView(priceView);

        // Seller info
        TextView sellerView = new TextView(ctx);
        sellerView.setText(isOwnListing ? "This is your listing" : "Sold by: " + listing.sellerUsername);
        sellerView.setTextColor(isOwnListing ? Color.parseColor("#44BBFF") : Color.parseColor("#AAAACC"));
        sellerView.setTextSize(12);
        sellerView.setGravity(Gravity.CENTER);
        dialogContent.addView(sellerView);

        // Currently owned count
        int owned = SaveManager.getInstance().getVaultItemCount(listing.itemId);
        if (owned > 0) {
            TextView ownedView = new TextView(ctx);
            ownedView.setText("You own: x" + owned);
            ownedView.setTextColor(Color.parseColor("#AAAACC"));
            ownedView.setTextSize(12);
            ownedView.setGravity(Gravity.CENTER);
            dialogContent.addView(ownedView);
        }

        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(dialogScroll);
        if (!isOwnListing) {
            boolean canAfford = SaveManager.getInstance().getData().coins >= listing.price;
            if (canAfford) {
                builder.setPositiveButton(CoinIconHelper.withCoin(ctx,
                        "Buy (\u25C8 " + listing.price + ")", 14), (dialog, which) -> {
                    purchaseMarketplaceItem(listing, template);
                });
            }
        }
        builder.setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    private void showBuyConfirmation(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int coins = SaveManager.getInstance().getData().coins;

        if (coins < shopItem.price) {
            Toast.makeText(ctx, CoinIconHelper.withCoin(ctx,
                    "Not enough coins! Need \u25C8 " + shopItem.price, 14), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(ctx)
                .setTitle("Buy " + template.getName() + "?")
                .setMessage(CoinIconHelper.withCoin(ctx,
                        "Cost: \u25C8 " + shopItem.price + " coins\nYour balance: \u25C8 " + coins + " coins\n\nAfter purchase: \u25C8 " + (coins - shopItem.price) + " coins", 14))
                .setPositiveButton("Buy", (dialog, which) -> purchaseItem(shopItem, template))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show buy confirmation for a player marketplace listing.
     */
    private void showMarketplaceBuyConfirmation(SaveData.PlayerListing listing, Item template) {
        Context ctx = getContext();
        int coins = SaveManager.getInstance().getData().coins;

        if (coins < listing.price) {
            Toast.makeText(ctx, CoinIconHelper.withCoin(ctx,
                    "Not enough coins! Need \u25C8 " + listing.price, 14), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(ctx)
                .setTitle("Buy " + template.getName() + "?")
                .setMessage(CoinIconHelper.withCoin(ctx,
                        "Cost: \u25C8 " + listing.price + " coins\nSold by: " + listing.sellerUsername + "\nYour balance: \u25C8 " + coins + " coins\n\nAfter purchase: \u25C8 " + (coins - listing.price) + " coins\n\nCoins will go to " + listing.sellerUsername + ".", 14))
                .setPositiveButton("Buy", (dialog, which) -> purchaseMarketplaceItem(listing, template))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void purchaseItem(SaveData.ShopItem shopItem, Item template) {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.spendCoins(shopItem.price)) {
            Toast.makeText(getContext(), "Not enough coins!", Toast.LENGTH_SHORT).show();
            return;
        }

        sm.addVaultItem(shopItem.itemId, 1);
        sm.getData().totalItemsCollected++;
        sm.save();

        Toast.makeText(getContext(), "Purchased " + template.getName() + "!", Toast.LENGTH_SHORT).show();

        if (getContext() instanceof TabActivity) {
            ((TabActivity) getContext()).updateCoinDisplay();
        }
        refreshShop();
    }

    /**
     * Purchase a player marketplace listing. Coins go to the seller.
     */
    private void purchaseMarketplaceItem(SaveData.PlayerListing listing, Item template) {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.purchasePlayerListing(listing)) {
            Toast.makeText(getContext(), "Purchase failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Purchased " + template.getName() + " from " + listing.sellerUsername + "!", Toast.LENGTH_SHORT).show();

        if (getContext() instanceof TabActivity) {
            ((TabActivity) getContext()).updateCoinDisplay();
        }
        refreshShop();
    }

    private void updateBalance() {
        SaveManager sm = SaveManager.getInstance();
        CoinIconHelper.setCoinText(balanceText,
                "Your Coins: \u25C8 " + sm.getData().coins, 16);
    }
}
