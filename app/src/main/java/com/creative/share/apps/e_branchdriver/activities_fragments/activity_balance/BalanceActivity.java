package com.creative.share.apps.e_branchdriver.activities_fragments.activity_balance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.creative.share.apps.e_branchdriver.R;
import com.creative.share.apps.e_branchdriver.adapters.ChargeAdapter;
import com.creative.share.apps.e_branchdriver.databinding.ActivityBalanceBinding;
import com.creative.share.apps.e_branchdriver.interfaces.Listeners;
import com.creative.share.apps.e_branchdriver.language.LanguageHelper;
import com.creative.share.apps.e_branchdriver.models.BalanceModel;
import com.creative.share.apps.e_branchdriver.models.CopunModel;
import com.creative.share.apps.e_branchdriver.models.UserModel;
import com.creative.share.apps.e_branchdriver.preferences.Preferences;
import com.creative.share.apps.e_branchdriver.remote.Api;
import com.creative.share.apps.e_branchdriver.share.Common;
import com.creative.share.apps.e_branchdriver.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BalanceActivity extends AppCompatActivity implements Listeners.BackListener {
    private ActivityBalanceBinding binding;
    private String lang;
    private Preferences preferences;
    private UserModel userModel;
    private String copoun_num;
    private List<CopunModel> copunModelList;
    private ChargeAdapter adapter;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", Locale.getDefault().getLanguage())));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_balance);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent!=null&&intent.hasExtra("identity"))
        {
            copoun_num = intent.getStringExtra("identity");
        }
    }

    private void initView() {
        copunModelList = new ArrayList<>();
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang",Locale.getDefault().getLanguage());
        binding.setBackListener(this);
        binding.setLang(lang);
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        adapter = new ChargeAdapter(copunModelList,this);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(adapter);
        binding.btnCharge.setOnClickListener(view -> addBalance());
        getBalance();

    }
    private void addBalance()
    {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        try {

            Api.getService(Tags.base_url)
                    .addBalance(userModel.getId(),copoun_num)
                    .enqueue(new Callback<CopunModel>() {
                        @Override
                        public void onResponse(Call<CopunModel> call, Response<CopunModel> response) {
                            dialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(BalanceActivity.this,getString(R.string.suc), Toast.LENGTH_SHORT).show();
                                getBalance();
                            } else {

                                try {

                                    Log.e("error", response.code() + "_" + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (response.code() == 500) {
                                    Toast.makeText(BalanceActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                                } else if (response.code() == 422) {
                                    Toast.makeText(BalanceActivity.this, R.string.coupon_not_av, Toast.LENGTH_SHORT).show();


                                }
                                else {
                                    Toast.makeText(BalanceActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<CopunModel> call, Throwable t) {
                            try {
                                dialog.dismiss();
                                if (t.getMessage() != null) {
                                    Log.e("error", t.getMessage());
                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(BalanceActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(BalanceActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                            }
                        }
                    });
        } catch (Exception e) {
            dialog.dismiss();

        }
    }

    private void getBalance()
    {
        Log.e("id",userModel.getId()+"_");
        binding.progBar.setVisibility(View.VISIBLE);
        Api.getService(Tags.base_url)
                .getBalance(userModel.getId())
                .enqueue(new Callback<BalanceModel>() {
                    @Override
                    public void onResponse(Call<BalanceModel> call, Response<BalanceModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            updateUi(response.body());
                        } else {

                            try {

                                Log.e("error", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code() == 500) {
                                Toast.makeText(BalanceActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(BalanceActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BalanceModel> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(BalanceActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(BalanceActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateUi(BalanceModel balanceModel)
    {
        Log.e("balance",balanceModel.getTotal_balance()+"__");
        binding.setModel(balanceModel);
        copunModelList.clear();
        copunModelList.addAll(balanceModel.getPayments());
        adapter.notifyDataSetChanged();
        if (balanceModel.getPayments().size()>0)
        {
            binding.tvNoCharge.setVisibility(View.GONE);
        }else
            {
                binding.tvNoCharge.setVisibility(View.VISIBLE);

            }

    }

    @Override
    public void back() {
        finish();
    }
}
