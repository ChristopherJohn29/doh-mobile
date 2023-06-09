package com.example.pc.doh.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import com.example.pc.doh.Adapter.ExpandableListAdapter;

import com.example.pc.doh.Adapter.assessdetadapter;
import com.example.pc.doh.DatabaseHelper;
import com.example.pc.doh.InternetCheck;

import com.example.pc.doh.Model.MenuModel;
import com.example.pc.doh.Model.UserModel;
import com.example.pc.doh.Model.showassessitem;
import com.example.pc.doh.R;
import com.example.pc.doh.SharedPrefManager;
import com.example.pc.doh.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ShowAssessment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    TextView lblfacname;

    int menuindex = 0;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private ExpandableListAdapter expandableListAdapter;
    private String uid,uname;
    TextView address,review,header,items,ansitems;
    DatabaseHelper db;
    InternetCheck checker;
    Button btnsubmit,btndraft;
    Spinner spinner;


    //

    TextView tooltitle;
    //list item initialize
    RecyclerView rv;
    List<showassessitem> filteredList = new ArrayList<>();
    List<showassessitem> assessmentList = new ArrayList<>();
    assessdetadapter adapter;
    LinearLayout lcomment;
    String position = "";

    String hid,hdesc,pid,pdesc;

    private final String[] filterChoice = {"All", "YES", "NO", "N/A", "UnAnswered"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showassessment);

        UserModel user = SharedPrefManager.getInstance(this).getUser();
        uid = user.getId();
        uname = user.getName();
        Log.d("uname",uname);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        db = new DatabaseHelper(this);
        checker = new InternetCheck(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        rv = findViewById(R.id.sassesslist);
        if(AssessmentPart.hid!=null){
            Log.d("found","true");
            hid = AssessmentPart.hid;
            hdesc = AssessmentPart.hdesc;
            Log.d("hid",hid);
            Log.d("hdesc",hdesc);
        }else{
            hid = AssessmentHeaderOne.id;
            hdesc = AssessmentHeaderOne.desc;
            Log.d("found","false");
            Log.d("hid",hid);
            Log.d("hdesc",hdesc);
        }







        String json = db.get_user_json_data(uid);
        Log.d("json",json);
        try {
            JSONObject obj = new JSONObject(json);
            position = obj.getJSONObject("data").getString("position");
            //Log.d("position",obj.getJSONObject("data").getString("position"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        expandableListView = findViewById(R.id.expandableListView);
        lblfacname = findViewById(R.id.facname);
        tooltitle = findViewById(R.id.toolbar_title);
        address = findViewById(R.id.address);
        review = findViewById(R.id.review);
        header = findViewById(R.id.headercat);
        items = findViewById(R.id.items);
        ansitems = findViewById(R.id.ansitems);
        btnsubmit = findViewById(R.id.btnsubmit);
        lcomment = findViewById(R.id.lcomments);
        lcomment.setVisibility(View.GONE);
        btndraft = findViewById(R.id.btndraft);
        spinner = findViewById(R.id.spinner);
//        btndraft.setVisibility(View.GONE);
//        btnsubmit.setVisibility(View.GONE);
        btndraft.setEnabled(false);
        btndraft.setAlpha(.5f);
        btnsubmit.setEnabled(false);
        btnsubmit.setAlpha(.5f);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(ShowAssessment.this,
                android.R.layout.simple_spinner_item, filterChoice);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        tooltitle.setText("Assessment Details");
        tooltitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssessmentPart.hid = null;
                AssessmentPart.hdesc = null;
                Intent headerthree = new Intent(getApplicationContext(), AssessmentHeaderOne.class);
                startActivity(headerthree);
                Animatoo.animateSlideRight(ShowAssessment.this);
                //Toast.makeText(getApplicationContext(),"ENTRIES SAVE AS DRAFT",Toast.LENGTH_SHORT).show();
            }
        });

        lblfacname.setText(HomeActivity.faclityname);

        review.setVisibility(View.GONE);
        header.setText(AssessmentPart.desc + "    >    "+hdesc);
        prepareMenuData();
        populateExpandableList();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.txtempname);
        navUsername.setText(user.getName());


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                    drawer.closeDrawer(Gravity.RIGHT);
                } else {
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

//        btnsubmit.startAnimation();


        //instanstiate list


        adapter = new assessdetadapter(this, filteredList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

//        adapter.addItemTouchListener(new assessdetadapter.onTouchListener() {
//            @Override
//            public void onTouch(int position) {
//                int size = silist.size() - 1;
//                save_assessment(position);
//                if(size == position)
//                {
//                    btnsubmit.setVisibility(View.VISIBLE);
//                    btndraft.setVisibility(View.VISIBLE);
//                }else{
//                    btnsubmit.setVisibility(View.GONE);
//                    btndraft.setVisibility(View.GONE);
//                }
//                Log.d("touch",position+"");
//                int page = position + 1;
//                items.setText(page+" of "+silist.size());
//                //,,
//                ansitems.setText("Answer item(s) : "+db.countanswer(AssessmentPart.id,hid,HomeActivity.appid));
//            }
//        });

        adapter.addClickRadioListener(new assessdetadapter.OnClickRadioListener() {
            @Override
            public void onClickRadio(int position) {
                save_assessment(position);
                ansitems.setText("Answer item(s) : "+db.countanswer(AssessmentPart.id,hid,HomeActivity.appid) + " of " + assessmentList.size());

                btndraft.setEnabled(true);
                btndraft.setAlpha(1);

                boolean hasUnAnswered = false;
                for (int c = 0; c < assessmentList.size(); c++) {
                    String choice = assessmentList.get(c).getChoice();
                    if (choice == null || choice.length() == 0 || choice.equals("-1")) {
                        hasUnAnswered = true;
                        break;
                    }
                }

                if (!hasUnAnswered) {
                    btnsubmit.setEnabled(true);
                    btnsubmit.setAlpha(1);
                }
            }
        });

        adapter.addRemarksTextChangeListener(new assessdetadapter.OnRemarksTextChangeListener() {
            @Override
            public void onRemarksTextChange(final int position) {
                // needs to be optimized
//                new Timer().schedule(
//                        new TimerTask() {
//                            @Override
//                            public void run() {
//                                save_assessment(position);
//
//                                btndraft.setEnabled(true);
//                                btndraft.setAlpha(1);
//                            }
//                        },
//                        500
//                );
                save_assessment(position);

                btndraft.setEnabled(true);
                btndraft.setAlpha(1);
            }
        });


        retrieveassessdetails();
        ansitems.setText("Answer item(s) : "+db.countanswer(AssessmentPart.id,hid,HomeActivity.appid) + " of " + assessmentList.size());
//        if(checker.checkHasInternet()){
//            Log.d("internet","true");
//            get_showassessment();
//        }else{
//            retrieveassessdetails();
//            Log.d("internet","false");
//        }


        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(filteredList.size() == 1){
                    save_assessment(0);
                    save_assessment_header();
                }else{
                    //menu.getItem(0).setVisible(false);
                    boolean check = false;
                    boolean skipcheck = false;
                    //check if all results are answer
                    int index = 0;
                    for(int i = 0; i< filteredList.size(); i++){
                        if(filteredList.get(i).getChoice().equals("")){
                            Log.d("choice","true");
                            index = i;
                            check = true;
                            break;
                        }
                        else if(filteredList.get(i).getChoice().equals("SKIP"))
                        {
                            skipcheck = true;
                        }
                    }
                    final int pos = index;
                    if(check){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowAssessment.this);
                        builder.setTitle("DOHOLRS");
                        builder.setMessage("Please answer all assessments.");
                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rv.scrollToPosition(pos);
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.doh);
                        dialog.show();
                    }else if(skipcheck){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowAssessment.this);
                        builder.setTitle("DOHOLRS");
                        builder.setMessage("Some questions are skip.Please save as draft.");
                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //rv.scrollToPosition(pos);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.doh);
                        dialog.show();
                    }else {
                        save_assessment_header();
                    }

                }
            }
        });

        btndraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AssessmentPart.hid = null;
                AssessmentPart.hdesc = null;
                Intent headerthree = new Intent(getApplicationContext(), AssessmentHeaderOne.class);
                startActivity(headerthree);
                Animatoo.animateSlideRight(ShowAssessment.this);
                Toast.makeText(getApplicationContext(),"ENTRIES SAVE AS DRAFT",Toast.LENGTH_SHORT).show();
                finish();
            }
        });



//        headerrv = findViewById(R.id.headerrv);
//        hAdapter = new HeadersAdapter(this, list);
//        headerrv.setLayoutManager(new LinearLayoutManager(this));
//        headerrv.setAdapter(hAdapter);
//        hAdapter.setonItemClickListener(new HeadersAdapter.onItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                //id = list.get(position).getAsmt2l_id();
//                //Intent showassess = new Intent(getApplicationContext(), ShowAssessment.class);
//                //(showassess);
//
//            }
//        });
        items.setText("1 of "+ filteredList.size());
    }

    @Override
    public void onBackPressed() {
        AssessmentPart.hid = null;
        AssessmentPart.hdesc = null;
        super.onBackPressed();
    }

    private void saveAssessment(int pos, String remarks) {
        String choice = filteredList.get(pos).getChoice();

//        if (remarks != null && remarks.length() == 0) {
//            filteredList.get(pos).setRemarks(remarks);
//        }

        try {
            if ((choice != null && choice.length() > 0) || remarks != null) {
                String dupid = db.get_tbl_assesscombinedheaderone(HomeActivity.appid,uid, filteredList.get(pos).getId(),hid);
                if (db.checkDatas("assesscombined", "dupID", dupid)){
                    Log.d("check","true");
                    Boolean check = db.get_tbl_assesscombineduid(dupid,uid);
                    if(check){
                        String[] ucolumns = {"evaluation","remarks"};
                        String[] udata = {choice,remarks};
                        if (db.update("assesscombined", ucolumns, udata, "dupid",dupid)) {
                            Log.d("updatedata", "update");
                            //Toast.makeText(getApplicationContext(),"Save Answer",Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("updatedata", "not update");
                        }
                    }
                }else{
                    Log.d("check","false");
                    String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
                            "evaluation","remarks","evaluatedBy","appid","monid","epos","ename"};
                    String[] datas = {filteredList.get(pos).getId(), filteredList.get(pos).getDisp(), filteredList.get(pos).getSequence(), filteredList.get(pos).getOtherheading(),
                            AssessmentPart.id,AssessmentPart.desc,AssessmentHeaderOne.asmt2id,AssessmentHeaderOne.asmt2desc,
                            hid,hdesc,hid,choice,remarks,uid,HomeActivity.appid,"",position,uname};
                    if (db.add("assesscombined", dcolumns, datas, "")) {
                        Log.d("assesscombined", "added");
                        //Toast.makeText(getApplicationContext(),"Save Answer",Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("assesscombined", "not added");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void save_assessment(int pos){
        View view = rv.findViewWithTag(pos);
        if(view != null) {
//            EditText remark = view.findViewById(R.id.remark);
            saveAssessment(pos, filteredList.get(pos).getRemarks());
        }
    }

//    private void save_assessment(int pos){
//        View view =rv.findViewWithTag(pos);
//        if(view!=null){
//            EditText txtr = view.findViewById(R.id.remark);
//            RadioGroup r = view.findViewById(R.id.rgchoice);
//            int selectedId = r.getCheckedRadioButtonId();
//            String choice = "";
//            //rv.setNestedScrollingEnabled(true);
//            //Log.d("position",position);
//            switch (selectedId){
//                case R.id.yes:
//                    choice = "1";
//                    //txtr.setError(null);
//                    silist.get(pos).setChoice("1");
//                    rv.setLayoutFrozen(false);
//                    break;
//                case R.id.no:
//                    choice = "0";
//                    silist.get(pos).setChoice("0");
//                    rv.setLayoutFrozen(false);
//                    /*
//                    if(TextUtils.isEmpty(txtr.getText().toString())){
//                        //txtr.setError("Please Enter your Remarks. Thank You");
//                        //rv.setNestedScrollingEnabled(false);
//                        //return;
//                    }*/
//                    break;
//                case R.id.na:
//                    silist.get(pos).setChoice("NA");
//                    choice = "NA";
//                    rv.setLayoutFrozen(false);
//                    break;
//                case R.id.skip:
//                    silist.get(pos).setChoice("SKIP");
//                    choice = "SKIP";
//                    rv.setLayoutFrozen(false);
//                    break;
//                default:
//                    choice = "nochoice";
//                    //rv.setNestedScrollingEnabled(false);
//                    Toast.makeText(getApplicationContext(),"Please Choose your Answer",Toast.LENGTH_SHORT).show();
//                    rv.scrollToPosition(pos);
//                    rv.setLayoutFrozen(true);
//                    break;
//            }
//
//            if(txtr.getText().toString() != null && !txtr.getText().toString().equals("")){
//                Log.d("remark",txtr.getText().toString());
//                silist.get(pos).setRemarks(txtr.getText().toString());
//            }
//            Log.d("choice",choice);
//            Log.d("id",silist.get(pos).getId());
//            Log.d("desc",silist.get(pos).getDisp());
//            Log.d("otherheading",silist.get(pos).getOtherheading());
//            Log.d("sequence",silist.get(pos).getSequence());
//            //
//            if(choice != "nochoice"){
//                String dupid = db.get_tbl_assesscombinedheaderone(HomeActivity.appid,uid,silist.get(pos).getId(),hid);
//                if (db.checkDatas("assesscombined", "dupID", dupid)){
//                      Log.d("check","true");
//                    Boolean check = db.get_tbl_assesscombineduid(dupid,uid);
//                    if(check){
//                        String[] ucolumns = {"evaluation","remarks"};
//                        String[] udata = {choice,txtr.getText().toString()};
//                        if (db.update("assesscombined", ucolumns, udata, "dupid",dupid)) {
//                            Log.d("updatedata", "update");
//                            //Toast.makeText(getApplicationContext(),"Save Answer",Toast.LENGTH_SHORT).show();
//                        } else {
//                            Log.d("updatedata", "not update");
//                        }
//                    }
//                }else{
//                    Log.d("check","false");
//                    String[] dcolumns = {"asmtComb_FK", "assessmentName", "assessmentSeq","assessmentHead","asmtH3ID_FK","h3name","asmtH2ID_FK","h2name","asmtH1ID_FK","h1name","partID",
//                                         "evaluation","remarks","evaluatedBy","appid","monid","epos","ename"};
//                    String[] datas = {silist.get(pos).getId(),silist.get(pos).getDisp(),silist.get(pos).getSequence(),silist.get(pos).getOtherheading(),
//                                      AssessmentPart.id,AssessmentPart.desc,AssessmentHeaderOne.asmt2id,AssessmentHeaderOne.asmt2desc,
//                                      hid,hdesc,hid,choice,txtr.getText().toString(),uid,HomeActivity.appid,"",position,uname};
//                    if (db.add("assesscombined", dcolumns, datas, "")) {
//                        Log.d("assesscombined", "added");
//                        //Toast.makeText(getApplicationContext(),"Save Answer",Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.d("assesscombined", "not added");
//                    }
//                }
//            }else{
//
//            }
//        }
//    }

    private void save_assessment_header(){
        String id = db.get_tbl_assessment_header(HomeActivity.appid,uid,hid,"3");
        if (db.checkDatas("tbl_save_assessment_header", "assessheadid", id)){
//            String[] ucolumns = {"choice","remarks"};
//            String[] udata = {choice,txtr.getText().toString()};
//            if (db.update("tbl_save_assessment_res", ucolumns, udata, "resid",id)) {
//                Log.d("updatedata", "update");
//            } else {
//                Log.d("updatedata", "not update");
//            }
        }else{
            String[] dcolumns = {"headerid", "headerlevel", "assess","appid","uid"};
            String[] datas = {hid,"1","true",HomeActivity.appid,uid};
            if (db.add("tbl_save_assessment_header", dcolumns, datas, "")) {
                Log.d("tblsaveassessheader", "added");
            } else {
                Log.d("tblsaveassessheader", "not added");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowAssessment.this);
            builder.setTitle("DOHOLRS");
            builder.setMessage("Successfully Save Assessment Results");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AssessmentPart.hid = null;
                    AssessmentPart.hdesc = null;
                    Intent header = new Intent(ShowAssessment.this,AssessmentHeaderOne.class);
                    startActivity(header);
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setIcon(R.drawable.doh);
            dialog.show();
        }

    }

    private void retrieveassessdetails(){
        Log.d("retrieve","execute");
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        String sid = db.getsid(HomeActivity.appid,"",hid);
        Cursor det = db.get_item("tbl_show_assessment", "sid", sid);
        Log.d("sid", sid);
        Log.d("sid", det.toString());
        boolean checkifcomplied = false;
        if (det != null && det.getCount() > 0) {
            det.moveToFirst();
            while (!det.isAfterLast()) {
                String json = det.getString(det.getColumnIndex("json_data"));
                Log.d("json",json);
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject data = obj.getJSONObject("data");
                    JSONArray head = obj.getJSONArray("head");
                    //Log.d("head",head.toString());
                    String addr = data.getString("streetname") +", "+ data.getString("brgyname") + ", "+
                            data.getString("cmname") +", "+ data.getString("provname");
                    address.setText(addr);
                    if(head.length()>0){
                        for(int i =0;i<head.length();i++){
                            String id = head.getJSONObject(i).getString("id");
                            String desc =head.getJSONObject(i).getString("description");
                            String otherhead = "";
                            String seq = head.getJSONObject(i).getString("sequence");
                            String choice = "";
                            String remarks = "";
                            if(!head.getJSONObject(i).isNull("otherHeading")){
                                Log.d("otherheading"," not null");
                                otherhead = head.getJSONObject(i).getString("otherHeading");
                                desc = head.getJSONObject(i).getString("description");
                            }
                            Log.d("headid",id);

                            String dupid = db.get_tbl_assesscombinedheaderone(HomeActivity.appid,uid,id,hid);
                            if(!dupid.equals("")){
                                Log.d("dupid","true");
                                if (db.checkDatas("assesscombined", "dupID", dupid)){
                                    choice = db.get_tbl_assesscombinedevaluation(HomeActivity.appid,uid,id,hid);
                                    remarks = db.get_tbl_assesscombinedremarks(HomeActivity.appid,uid,id,hid);
                                }
                            }
                            assessmentList.add(new showassessitem(desc,choice,remarks,id,otherhead,seq));
                            filteredList.add(new showassessitem(desc,choice,remarks,id,otherhead,seq));
                        }
                    }else{

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
                sv.setVisibility(View.VISIBLE);
                btnsubmit.setVisibility(View.VISIBLE);
                det.moveToNext();
            }
        } else {
            TextView lbl = findViewById(R.id.lblheadmessage);
            //lbl.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            sv.setVisibility(View.VISIBLE);
        }

    }

    private void get_showassessment(){
        final ProgressBar bar = findViewById(R.id.asesssProgress);
        final ScrollView sv = findViewById(R.id.svheader);
        bar.setVisibility(View.VISIBLE);
        bar.getIndeterminateDrawable().setColorFilter(Color.rgb(40, 165, 95), PorterDuff.Mode.SRC_IN);
        sv.setVisibility(View.GONE);
        Log.d("getshowassessment","execute");

        StringRequest request = new StringRequest(Request.Method.POST, Urls.getassessmentdet+HomeActivity.appid+"/"+hid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responsess",response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.getJSONObject("data");
                            JSONArray head = obj.getJSONArray("head");
                            String sid = db.getsid(HomeActivity.appid,"",hid);
                            if (db.checkDatas("tbl_show_assessment", "sid", sid)){
                                String[] ucolumns = {"json_data"};
                                String[] udata = {response};
                                if (db.update("tbl_show_assessment", ucolumns, udata, "sid",sid)) {
                                    Log.d("updatedata", "update");
                                } else {
                                    Log.d("updatedata", "not update");
                                }
                            }else{
                                String[] dcolumns = {"json_data", "uid", "appid","id","monid"};
                                String[] datas = {response, uid, HomeActivity.appid,hid,""};
                                if (db.add("tbl_show_assessment", dcolumns, datas, "")) {
                                    Log.d("tbl_show_assessment", "added");
                                } else {
                                    Log.d("tbl_show_assessment", "not added");
                                }
                            }
                            //Log.d("head",head.toString());
                            String addr = data.getString("streetname") +", "+ data.getString("brgyname") + ", "+
                                  data.getString("cmname") +", "+ data.getString("provname");
                            address.setText(addr);
                            if(head.length()>0){
                                for(int i =0;i<head.length();i++){
                                    String id = head.getJSONObject(i).getString("id");
                                    String desc =head.getJSONObject(i).getString("description");
                                    String otherhead = "";
                                    String seq = head.getJSONObject(i).getString("sequence");
                                    String choice = "";
                                    String remarks = "";
                                    if(!head.getJSONObject(i).isNull("otherHeading")){
                                      Log.d("otherheading"," not null");
                                      otherhead = head.getJSONObject(i).getString("otherHeading");
                                      desc = head.getJSONObject(i).getString("description");
                                    }
                                    String dupid = db.get_tbl_assesscombinedheaderone(HomeActivity.appid,uid,id,hid);
                                    if(!dupid.equals("")){
                                      Log.d("dupid","true");
                                        if (db.checkDatas("assesscombined", "dupID", dupid)){
                                        choice = db.get_tbl_assesscombinedevaluation(HomeActivity.appid,uid,id,hid);
                                        remarks = db.get_tbl_assesscombinedremarks(HomeActivity.appid,uid,id,hid);
                                        }
                                    }
                                    filteredList.add(new showassessitem(desc,choice,remarks,id,otherhead,seq));
                                }
                            }else{

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        btnsubmit.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                        sv.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("isMobile","dan");
                params.put("uid",uid);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    private void prepareMenuData() {

        MenuModel menuModel;
        ///Drawable img = getAlContext().getResources().getDrawable( R.drawable.smiley );
        menuModel = new MenuModel("Licensing Process", true, true,R.drawable.ic_sitemap,0);
        headerList.add(menuModel);
        List<MenuModel> childModelsList = new ArrayList<>();
        MenuModel childModel = new MenuModel("Assessment Tool", false, false,0,0);
        childModelsList.add(childModel);
        childModel = new MenuModel("Evaluation Tool", false, false,0,1);
        childModelsList.add(childModel);
        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Monitoring", true, true,R.drawable.ic_desktop_windows_black_24dp,1);
        headerList.add(menuModel);
        childModel = new MenuModel("Monitoring Tool", false, false,0,0);
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Account Settings", true, true,R.drawable.ic_settings_black_24dp,3);
        headerList.add(menuModel);

        if(checker.checkHasInternet()){
            childModel = new MenuModel("Change Password", false, false,0,0);
            childModelsList.add(childModel);

        }else{
            childModel = new MenuModel("Change Pin Password", false, false,0,1);
            childModelsList.add(childModel);


        }

        if(db.check_has_pin_password(uid)){
            childModel = new MenuModel("Set Pin Password", false, false,0,2);
            childModelsList.add(childModel);
        }






        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        menuModel = new MenuModel("Logout", false, false,R.drawable.ic_power_settings_new_black_24dp,4);
        headerList.add(menuModel);


    }

    private void populateExpandableList() {

        expandableListAdapter = new ExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        onBackPressed();
                    }
                }


                switch (headerList.get(groupPosition).index){
                    case 4:
                        Intent main = new Intent(ShowAssessment.this,MainActivity.class);
                        startActivity(main);
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        finish();
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Animatoo.animateSlideRight(ShowAssessment.this);
                        break;
                   /* case 7:

                        break;*/
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    switch (headerList.get(groupPosition).index){
                        case 0:
                            if(model.index==1){
                                Intent evaluation = new Intent(getApplicationContext(),EvaluationActivity.class);
                                startActivity(evaluation);
                            }
                            if(model.index==0){
                                Intent home = new Intent(getApplicationContext(),HomeActivity.class);
                                startActivity(home);
                            }
                            break;
                        case 1:
                            if(model.index==0){
                                Intent monitor = new Intent(getApplicationContext(),MonitoringActivity.class);
                                startActivity(monitor);
                            }
                            break;
                        case 2:
                            if(model.index==0){
                               /* Intent changepass = new Intent(getApplicationContext(),ChangePasswordActivity.class);
                                startActivity(changepass);*/
                            }
                            break;
                        case 3:
                            if(model.index==0){
                                Intent changepass = new Intent(getApplicationContext(),ChangePasswordActivity.class);
                                startActivity(changepass);
                            }
                            if(model.index==1){
                                Intent changepass = new Intent(getApplicationContext(),ChangePinPasswordActivity.class);
                                startActivity(changepass);
                            }
//                            if(model.index==2){
//                                set_pinpassword();
//
//                            }
                            break;
                        case 5:
                            if(model.index==0){


                            }
                            if(model.index==1){

                            }//mongenerate
                            if(model.index==2){


                            }//mongenerate
                            break;
                        case 6:
                            if(model.index==0){
                                /*Intent send = new Intent(getApplicationContext(),senddataActivity.class);
                                startActivity(send);*/
                                menuindex = 0;


                            }
                            if(model.index==1){
                                menuindex = 1;

                            }
                            if(model.index==2){
                                menuindex = 2;

                            }
                            /*if(model.index==1){
                                Intent receive = new Intent(getApplicationContext(),receivedataActivity.class);
                                startActivity(receive);

                            }*/
                            break;
                        case 7:


                            break;

                    }
                    onBackPressed();

                }

                return false;
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        filteredList = new ArrayList<>();
        for (int c = 0; c < assessmentList.size(); c++) {
            showassessitem item = assessmentList.get(c);
            switch (i) {
                case 0: // All
                    filteredList.add(item);
                    break;
                case 1: // YES
                    if (item.getChoice().equals("1")) {
                        filteredList.add(item);
                    }
                    break;
                case 2: // NO
                    if (item.getChoice().equals("0")) {
                        filteredList.add(item);
                    }
                    break;
                case 3: // N/A
                    if (item.getChoice().equals("NA")) {
                        filteredList.add(item);
                    }
                    break;
                case 4: // UnAnswered
                    if (item.getChoice() == null || item.getChoice().length() == 0 || item.getChoice().equals("-1")) {
                        filteredList.add(item);
                    }
                    break;
            }
        }

        adapter.setList(filteredList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    @Override
//    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
//
//
//        return false;
//    }
//
//    @Override
//    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
//        Log.d("touch2","true");
//    }
//
//    @Override
//    public void onRequestDisallowInterceptTouchEvent(boolean b) {
//        Log.d("touch3","true");
//
//    }

}
