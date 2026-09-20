package com.mdiaz.trxlauncher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.NavigationView;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.Waypoint;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class NavigationScreen extends ScrollView {
    private final MainActivity a;
    private NavigationView navView;
    private FrameLayout mapFrame;
    private Navigator navigator;
    private GoogleMap map;
    private PlacesClient places;
    private int searchToken;
    private boolean guiding;
    private final LinearLayout content;
    private final LruCache<String, Bitmap> artCache = new LruCache<String, Bitmap>(48) {
        @Override protected int sizeOf(String key, Bitmap value) { return value != null ? 1 : 0; }
    };

    NavigationScreen(MainActivity c, Bundle state) {
        super(c);
        a = c;
        setFillViewport(true);
        setBackgroundColor(Ui.BG);

        content = Ui.col(c);
        content.setPadding(Ui.dp(c, 18), Ui.dp(c, 14), Ui.dp(c, 18), Ui.dp(c, 18));
        addView(content);

        content.addView(Ui.text(a, "PRECISION NAVIGATION", 11, Ui.RED, true));
        content.addView(Ui.text(a, "Live route, on the grid.", 28, Color.WHITE, true));
        content.addView(Ui.text(a, "Google Maps turn-by-turn with live traffic.", 14, 0xffa5a8ae, false));
        Ui.margins(content.getChildAt(2), 0, 0, 0, 8);

        LinearLayout tabs = Ui.tabs(a, new String[]{"Route", "Recents", "Favorites", "Map Options"}, 0, this::selectTab);
        content.addView(tabs, new LinearLayout.LayoutParams(-1, Ui.dp(a, 66)));
        Ui.margins(tabs, 0, 6, 0, 8);

        buildSearchBar();
        buildMapFrame(state);
        buildMapControls();
        buildMusicQueue();

        initGoogle(state);
    }

    private EditText search;
    private LinearLayout suggestions;

    private void buildSearchBar() {
        LinearLayout bar = Ui.col(a);
        search = new EditText(a);
        search.setSingleLine(true);
        search.setHint("Search destination or address");
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xffa1a5ac);
        search.setTextSize(16);
        search.setPadding(Ui.dp(a, 22), 0, Ui.dp(a, 22), 0);
        search.setBackground(Ui.bg(0xf207080a, 0xff565a61, 18, a));
        bar.addView(search, new LinearLayout.LayoutParams(-1, Ui.dp(a, 54)));

        suggestions = Ui.col(a);
        suggestions.setBackground(Ui.bg(0xf508090b, Ui.RED, 16, a));
        suggestions.setVisibility(GONE);
        bar.addView(suggestions, new LinearLayout.LayoutParams(-1, -2));

        content.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        Ui.margins(bar, 0, 0, 0, 10);
        wireSearch();
    }

    private void buildMapFrame(Bundle state) {
        mapFrame = new FrameLayout(a);

        GradientDrawable borderBg = new GradientDrawable();
        borderBg.setColor(0xff0c0d0f);
        borderBg.setCornerRadius(Ui.dp(a, 20));
        borderBg.setStroke(Ui.dp(a, 2), 0xff2a2d33);
        mapFrame.setBackground(borderBg);
        mapFrame.setPadding(Ui.dp(a, 3), Ui.dp(a, 3), Ui.dp(a, 3), Ui.dp(a, 3));

        navView = new NavigationView(a);
        navView.onCreate(state);
        GradientDrawable mapClip = new GradientDrawable();
        mapClip.setCornerRadius(Ui.dp(a, 18));
        navView.setClipToOutline(true);
        navView.setBackground(mapClip);
        mapFrame.addView(navView, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout mapLabel = Ui.col(a);
        mapLabel.setPadding(Ui.dp(a, 18), Ui.dp(a, 14), Ui.dp(a, 18), Ui.dp(a, 14));
        GradientDrawable labelBg = new GradientDrawable();
        labelBg.setColor(0xd9000000);
        labelBg.setCornerRadius(Ui.dp(a, 14));
        labelBg.setStroke(Ui.dp(a, 1), 0x55e1192d);
        mapLabel.setBackground(labelBg);
        TextView liveTag = Ui.text(a, "● LIVE MAP", 11, Ui.RED, true);
        mapLabel.addView(liveTag);
        FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
        labelLp.setMargins(Ui.dp(a, 14), Ui.dp(a, 14), 0, 0);
        mapFrame.addView(mapLabel, labelLp);

        Button end = Ui.button(a, "END ROUTE", true);
        end.setVisibility(GONE);
        end.setOnClickListener(v -> {
            if (navigator != null) navigator.stopGuidance();
            guiding = false;
            v.setVisibility(GONE);
            search.setVisibility(VISIBLE);
        });
        end.setTag("end");
        FrameLayout.LayoutParams endLp = new FrameLayout.LayoutParams(Ui.dp(a, 150), Ui.dp(a, 52), Gravity.BOTTOM | Gravity.RIGHT);
        endLp.setMargins(0, 0, Ui.dp(a, 16), Ui.dp(a, 16));
        mapFrame.addView(end, endLp);

        content.addView(mapFrame, new LinearLayout.LayoutParams(-1, Ui.dp(a, 380)));
    }

    private void buildMapControls() {
        LinearLayout panel = Ui.col(a);
        panel.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
        panel.setBackground(Ui.bg(Ui.CARD, 0xff2a2d33, 18, a));

        TextView label = Ui.text(a, "MAP CONTROLS", 11, 0xff9ea2a9, true);
        panel.addView(label);
        Ui.margins(label, 0, 0, 0, 8);

        LinearLayout row = Ui.row(a);

        String[] names = {"Road", "Satellite", "Hybrid", "Traffic", "2D / 3D"};
        for (int i = 0; i < names.length; i++) {
            final String n = names[i];
            Button b = Ui.button(a, n, false);
            b.setOnClickListener(v -> {
                if (n.equals("Road")) { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_NORMAL); }
                else if (n.equals("Satellite")) { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); }
                else if (n.equals("Hybrid")) { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_HYBRID); }
                else if (n.equals("Traffic")) { if (map != null) map.setTrafficEnabled(!map.isTrafficEnabled()); }
                else if (n.equals("2D / 3D")) { cycleLayer(); }
            });
            row.addView(b, new LinearLayout.LayoutParams(0, Ui.dp(a, 52), 1));
            if (i < names.length - 1) Ui.margins(b, 0, 0, 6, 0);
        }
        panel.addView(row, new LinearLayout.LayoutParams(-1, -2));

        content.addView(panel, new LinearLayout.LayoutParams(-1, -2));
        Ui.margins(panel, 0, 8, 0, 10);
    }

    private LinearLayout queueContainer;

    private void buildMusicQueue() {
        LinearLayout section = Ui.col(a);
        section.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
        section.setBackground(Ui.bg(Ui.CARD, 0xff2a2d33, 18, a));

        LinearLayout header = Ui.row(a);
        header.addView(Ui.text(a, "QUEUED MUSIC", 11, Ui.RED, true), new LinearLayout.LayoutParams(0, -1, 1));
        TextView source = Ui.text(a, "—", 11, 0xff9ea2a9, true);
        source.setTag("queue_source");
        header.addView(source);
        section.addView(header);
        Ui.margins(header, 0, 0, 0, 10);

        LinearLayout nowRow = Ui.row(a);
        nowRow.setTag("now_row");
        nowRow.setPadding(Ui.dp(a, 14), Ui.dp(a, 12), Ui.dp(a, 14), Ui.dp(a, 12));
        nowRow.setBackground(Ui.bg(0xff17191c, 0xff3a3d43, 16, a));

        ImageView art = new ImageView(a);
        art.setTag("now_art");
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable placeholder = new GradientDrawable();
        placeholder.setColor(0xff222529);
        placeholder.setCornerRadius(Ui.dp(a, 10));
        art.setBackground(placeholder);
        nowRow.addView(art, new LinearLayout.LayoutParams(Ui.dp(a, 54), Ui.dp(a, 54)));

        LinearLayout info = Ui.col(a);
        info.setPadding(Ui.dp(a, 14), 0, 0, 0);
        TextView t = Ui.text(a, MediaAccessService.title, 16, Color.WHITE, true);
        t.setMaxLines(1);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setTag("now_title");
        info.addView(t);
        TextView ar = Ui.text(a, MediaAccessService.artist, 12, 0xffa9adb4, false);
        ar.setMaxLines(1);
        ar.setEllipsize(TextUtils.TruncateAt.END);
        ar.setTag("now_artist");
        info.addView(ar);

        Button play = Ui.button(a, MediaAccessService.playing ? "❚❚" : "▶", false);
        play.setTag("now_play");
        play.setOnClickListener(v -> {
            MediaAccessService.playPause();
            refreshMusicQueue();
        });
        nowRow.addView(info, new LinearLayout.LayoutParams(0, -1, 1));
        nowRow.addView(play, new LinearLayout.LayoutParams(Ui.dp(a, 52), Ui.dp(a, 52)));
        section.addView(nowRow, new LinearLayout.LayoutParams(-1, -2));
        Ui.margins(nowRow, 0, 0, 0, 10);

        queueContainer = Ui.col(a);
        queueContainer.setTag("queue_list");
        section.addView(queueContainer, new LinearLayout.LayoutParams(-1, -2));

        Button openMedia = Ui.button(a, "Open Media Source", false);
        openMedia.setOnClickListener(v -> a.showPage(2));
        section.addView(openMedia, new LinearLayout.LayoutParams(-1, Ui.dp(a, 52)));
        Ui.margins(openMedia, 0, 10, 0, 0);

        content.addView(section, new LinearLayout.LayoutParams(-1, -2));
        refreshMusicQueue();
    }

    private void refreshMusicQueue() {
        View nowRow = content.findViewWithTag("now_row");
        if (nowRow == null) return;

        TextView source = content.findViewWithTag("queue_source");
        if (source != null) {
            String pkg = MediaAccessService.sourcePkg;
            source.setText(pkg != null ? "OPEN IN " + pkg.replace("com.google.android.apps.", "").toUpperCase() : "NO SOURCE");
        }

        ImageView art = content.findViewWithTag("now_art");
        if (art != null) {
            Bitmap bm = MediaAccessService.albumArt;
            if (bm != null) {
                art.setImageBitmap(bm);
                art.setBackground(null);
            } else {
                art.setImageBitmap(null);
                GradientDrawable ph = new GradientDrawable();
                ph.setColor(0xff222529);
                ph.setCornerRadius(Ui.dp(a, 10));
                art.setBackground(ph);
            }
        }

        TextView t = content.findViewWithTag("now_title");
        if (t != null) t.setText(MediaAccessService.title);

        TextView ar = content.findViewWithTag("now_artist");
        if (ar != null) ar.setText(MediaAccessService.artist);

        Button play = content.findViewWithTag("now_play");
        if (play != null) play.setText(MediaAccessService.playing ? "❚❚" : "▶");

        queueContainer.removeAllViews();
        List<MediaAccessService.QueueEntry> q = MediaAccessService.queue;
        if (q == null || q.isEmpty()) {
            TextView empty = Ui.text(a, "Queue appears when a media app is playing.", 13, 0xff6b6e74, false);
            queueContainer.addView(empty, new LinearLayout.LayoutParams(-1, -2));
            Ui.margins(empty, 0, 4, 0, 0);
        } else {
            int shown = 0;
            for (MediaAccessService.QueueEntry e : q) {
                if (shown >= 8) break;
                LinearLayout row = Ui.row(a);
                row.setPadding(Ui.dp(a, 12), Ui.dp(a, 8), Ui.dp(a, 12), Ui.dp(a, 8));

                ImageView qArt = new ImageView(a);
                qArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (e.art != null) {
                    String key = e.title + "|" + e.artist;
                    Bitmap cached = artCache.get(key);
                    if (cached == null) { artCache.put(key, e.art); cached = e.art; }
                    qArt.setImageBitmap(cached);
                } else {
                    GradientDrawable ph = new GradientDrawable();
                    ph.setColor(0xff222529);
                    ph.setCornerRadius(Ui.dp(a, 8));
                    qArt.setBackground(ph);
                }
                row.addView(qArt, new LinearLayout.LayoutParams(Ui.dp(a, 40), Ui.dp(a, 40)));

                LinearLayout info = Ui.col(a);
                info.setPadding(Ui.dp(a, 12), 0, 0, 0);
                TextView qt = Ui.text(a, e.title, 14, Color.WHITE, false);
                qt.setMaxLines(1); qt.setEllipsize(TextUtils.TruncateAt.END);
                info.addView(qt);
                TextView qa = Ui.text(a, e.artist.isEmpty() ? "—" : e.artist, 11, 0xff7a7d83, false);
                qa.setMaxLines(1); qa.setEllipsize(TextUtils.TruncateAt.END);
                info.addView(qa);
                row.addView(info, new LinearLayout.LayoutParams(0, -1, 1));

                queueContainer.addView(row, new LinearLayout.LayoutParams(-1, -2));
                shown++;
            }
        }
    }

    private void initGoogle(Bundle state) {
        if (!Places.isInitialized())
            Places.initialize(a.getApplicationContext(), BuildConfig.MAPS_API_KEY);
        places = Places.createClient(a);
        navView.getMapAsync(g -> {
            map = g;
            try {
                map.setTrafficEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(false);
            } catch (SecurityException ignored) {}
        });
        NavigationApi.getNavigator(a, new NavigationApi.NavigatorListener() {
            public void onNavigatorReady(Navigator n) {
                navigator = n;
                navView.setNavigationUiEnabled(true);
                navView.setHeaderEnabled(true);
                navView.setEtaCardEnabled(true);
                navView.setRecenterButtonEnabled(true);
                navView.setSpeedometerEnabled(true);
                navView.setSpeedLimitIconEnabled(true);
            }
            public void onError(int code) {
                Toast.makeText(a, "Google Navigation error " + code, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void wireSearch() {
        search.setImeOptions(EditorInfo.IME_ACTION_GO);
        search.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_GO) { routeTo(search.getText().toString()); return true; }
            return false;
        });
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { predict(s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
    }

    private void predict(String q) {
        int token = ++searchToken;
        suggestions.removeAllViews();
        if (q.trim().length() < 2) { suggestions.setVisibility(GONE); return; }
        FindAutocompletePredictionsRequest req = FindAutocompletePredictionsRequest.builder()
            .setQuery(q).setCountries("US").build();
        places.findAutocompletePredictions(req)
            .addOnSuccessListener(result -> {
                if (token != searchToken) return;
                suggestions.removeAllViews();
                int count = 0;
                for (AutocompletePrediction p : result.getAutocompletePredictions()) {
                    if (count++ >= 6) break;
                    String primary = p.getPrimaryText(null).toString();
                    String secondary = p.getSecondaryText(null).toString();
                    Button b = Ui.button(a, primary + "\n" + secondary, false);
                    b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    b.setOnClickListener(v -> fetchAndRoute(p.getPlaceId(), primary));
                    suggestions.addView(b, new LinearLayout.LayoutParams(-1, Ui.dp(a, 60)));
                }
                suggestions.setVisibility(suggestions.getChildCount() > 0 ? VISIBLE : GONE);
            })
            .addOnFailureListener(x -> suggestions.setVisibility(GONE));
    }

    private void fetchAndRoute(String id, String title) {
        FetchPlaceRequest req = FetchPlaceRequest.builder(id,
            Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)).build();
        places.fetchPlace(req)
            .addOnSuccessListener(r -> {
                Place p = r.getPlace();
                LatLng ll = p.getLocation();
                if (ll != null) startGuidance(ll, title);
            })
            .addOnFailureListener(e -> Toast.makeText(a, "Destination unavailable", Toast.LENGTH_SHORT).show());
    }

    void routeTo(String query) {
        if (query == null || query.isBlank()) { search.requestFocus(); return; }
        search.setText(query);
        FindAutocompletePredictionsRequest req = FindAutocompletePredictionsRequest.builder()
            .setQuery(query).setCountries("US").build();
        places.findAutocompletePredictions(req)
            .addOnSuccessListener(r -> {
                if (r.getAutocompletePredictions().isEmpty())
                    Toast.makeText(a, "No destination found", Toast.LENGTH_SHORT).show();
                else fetchAndRoute(r.getAutocompletePredictions().get(0).getPlaceId(), query);
            });
    }

    private void startGuidance(LatLng ll, String title) {
        if (navigator == null) { Toast.makeText(a, "Navigation is initializing", Toast.LENGTH_SHORT).show(); return; }
        Waypoint w = new Waypoint.Builder().setLatLng(ll.latitude, ll.longitude)
            .setTitle(title).setVehicleStopover(true).build();
        RoutingOptions o = new RoutingOptions();
        o.travelMode(RoutingOptions.TravelMode.DRIVING);
        navigator.setDestination(w, o).setOnResultListener(status -> {
            if (status == Navigator.RouteStatus.OK) {
                navigator.startGuidance();
                guiding = true;
                search.setVisibility(GONE);
                suggestions.setVisibility(GONE);
                View e = mapFrame.findViewWithTag("end");
                if (e != null) e.setVisibility(VISIBLE);
            } else Toast.makeText(a, "Route unavailable: " + status, Toast.LENGTH_LONG).show();
        });
    }

    private void selectTab(int tab) {
        if (tab == 0) { search.setVisibility(VISIBLE); return; }
        if (tab == 1 || tab == 2) {
            suggestions.removeAllViews();
            String[] rows = tab == 1
                ? new String[]{"Recent destinations appear after your first route", "Home", "Work"}
                : new String[]{"Home", "Work", "Add favorite"};
            for (String s : rows) {
                Button b = Ui.button(a, s, false);
                b.setOnClickListener(v -> routeTo(s));
                suggestions.addView(b, new LinearLayout.LayoutParams(-1, 60));
            }
            suggestions.setVisibility(VISIBLE);
        } else showMapOptions();
    }

    private void showMapOptions() {
        suggestions.removeAllViews();
        String[] opts = {"Road map", "Satellite", "Hybrid", "Live traffic", "Avoid tolls", "Avoid highways", "Avoid ferries", "Day / Night / Auto"};
        for (String x : opts) {
            Button b = Ui.button(a, x, false);
            if (x.equals("Road map")) b.setOnClickListener(v -> { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_NORMAL); });
            if (x.equals("Satellite")) b.setOnClickListener(v -> { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); });
            if (x.equals("Hybrid")) b.setOnClickListener(v -> { if (map != null) map.setMapType(GoogleMap.MAP_TYPE_HYBRID); });
            if (x.equals("Live traffic")) b.setOnClickListener(v -> { if (map != null) map.setTrafficEnabled(!map.isTrafficEnabled()); });
            suggestions.addView(b, new LinearLayout.LayoutParams(-1, 60));
        }
        suggestions.setVisibility(VISIBLE);
    }

    private void cycleLayer() {
        if (map == null) return;
        int t = map.getMapType();
        map.setMapType(t == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
    }

    void refreshQueue() { refreshMusicQueue(); }

    void onStart() { if (navView != null) navView.onStart(); }
    void onResume() { if (navView != null) navView.onResume(); refreshMusicQueue(); }
    void onPause() { if (navView != null) navView.onPause(); }
    void onStop() { if (navView != null) navView.onStop(); }
    void onDestroy() { if (navView != null) navView.onDestroy(); }
    void onSave(Bundle b) { if (navView != null) navView.onSaveInstanceState(b); }
}
