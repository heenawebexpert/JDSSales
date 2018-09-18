package com.jdssale;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jdssale.Response.DataResponse;
import com.jdssale.Retrofit.JDSSaleService;
import com.jdssale.Retrofit.RestClient;
import com.jdssale.Utilities.BaseActivity;
import com.jdssale.Utilities.JDSSale;
import com.jdssale.Utilities.Preferences;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,DashboardFragment.OnFragmentInteractionListener ,QuatationFragment.OnFragmentInteractionListener,PendingPayFragment.OnFragmentInteractionListener,PayHistoryFragment.OnFragmentInteractionListener,SaleFragment.OnFragmentInteractionListener{
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nav_view)
    NavigationView navView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    Fragment fragment = null;
    CircleImageView dashboardImage;TextView dashboardName;
    TextView title;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        title = (TextView) toolbar.findViewById(R.id.text_view_toolbar);
        title.setText("");
        toolbar.setTitle(null);
        toolbar.bringToFront();
        toolbar.invalidate();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.menu_sale, this.getTheme());
        drawable.setTint(getResources().getColor(R.color.colorBrown));
        toggle.setHomeAsUpIndicator(drawable);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        drawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        navView.setItemIconTintList(null);
         dashboardImage = (CircleImageView) navView.getHeaderView(0).findViewById(R.id.cv_user);
         dashboardName = (TextView) navView.getHeaderView(0).findViewById(R.id.tv_user);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new DashboardFragment()).commit();
        }
        setHeader();
    }

    public void setTitle(String titletxt) {
        title.setText(titletxt);
    }

    private void setHeader() {
        dashboardName.setText(Preferences.getInstance().getUserName());
        Picasso.with(dashboardImage.getContext()).load(R.drawable.dash).into(dashboardImage);
    }



    public void logout(){
        final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(DashboardActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.drawable.appicon);
        alertDialogBuilder.setMessage("Are you sure you want to logout?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutApi();
                Preferences.getInstance().setLogIn(false);
                Preferences.getInstance().clearUserDetails();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawer.closeDrawer(GravityCompat.START);
                dialog.dismiss();
            }
        });
        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        pbutton.setTextColor(Color.parseColor("#000000"));
        nbutton.setTextColor(Color.parseColor("#000000"));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void logoutApi() {
        JDSSaleService jdsSaleService = RestClient.getInstance().getClient().create(JDSSaleService.class);
        showProgressbar("Loading", "Logging Out...");
        jdsSaleService.logout(Preferences.getInstance().getUserId()).
                enqueue(new Callback<DataResponse>() {
                    @Override
                    public void onResponse(Call<DataResponse> call, Response<DataResponse> response) {
                        if (response.body().getFlag()==1) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), response.body().getData(), Toast.LENGTH_SHORT).show();
                        }
                        hideProgressBar();
                    }

                    @Override
                    public void onFailure(Call<DataResponse> call, Throwable t) {
                        hideProgressBar();
                        Toast.makeText(getApplicationContext(), JDSSale.getInstance().SERVER_ERROR, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_home) {
            fragmentClass = DashboardFragment.class;
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_pay_history) {
            fragmentClass = PayHistoryFragment.class;

        }else if (id == R.id.nav_pend_pay) {
            fragmentClass = PendingPayFragment.class;

        } else if (id == R.id.nav_quotation) {
            fragmentClass = QuatationFragment.class;
        }
        else if (id== R.id.nav_sale)
        {
            fragmentClass = SaleFragment.class;
        }
        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void replaceFragment(Class fragmentClass){
        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }
    }
}
