package com.idcard.modernskyticketing1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements IdcardFragment.FragmentListener, DbFragment.DbFragListener, IdcardFragment2.ManualFragmentListner {

    private static String TAG = "MainActivity";

    private SharedPreferences sp;

    // 底部菜单6个Linearlayout
    private LinearLayout ll_idcard;
    private LinearLayout ll_idcard2;
    private LinearLayout ll_fp;
    private LinearLayout ll_face;
    private LinearLayout ll_db;
    private LinearLayout ll_black;
    private LinearLayout ll_about;

    private TextView tv_title;





    // 中间内容区域

    private IdcardFragment idcardFragment;
    private IdcardFragment2 idcardFragment2;
    private FpFragment fpFragment;
    private FaceFragment faceFragment;
    private DbFragment dbFragment;
    private BlackDbFragment blackFragment;
    private AboutFragment aboutFragment;


    private DatabaseAccess dbaccess;
    private List<TableInfo> showTables;
    private List<TableInfo> allPassTables;
    private int currentShowDate;




    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbaccess.close();


        IPowerManager mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        int version = Integer.parseInt(Build.VERSION.SDK);
        try{
            if (version == 22){
                mPower.SetCardPower(0);
                mPower.SetFingerPower(0);
            }else {
                HsUtlis.IDCardPonoff1();
                HsUtlis.UsbPonoff1();
            }
        }catch (Exception e){

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        sp = getSharedPreferences("config", MODE_PRIVATE);
        initView();
        initEvent();
        String str = sp.getString("version", null);

        /*
        if (str == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = View.inflate(MainActivity.this, R.layout.activity_version, null);
            final EditText et = (EditText) view.findViewById(R.id.et_version);
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String v = et.getText().toString().trim();
                    sp.edit().putString("version", v).commit();
//					VersionDao dao = new VersionDao(MainActivity.this);

                    initFragment(0);
                }
            });
            builder.setView(view);
            builder.setNegativeButton("取消", null);
            builder.create().show();
        }else {
            initFragment(0);

        }*/

//        initFragment(0);

        dbaccess = DatabaseAccess.getInstance(MainActivity.this);
        dbaccess.open();
        List<String> list = dbaccess.getTables();
        TextView tv_ticketInfo = (TextView) findViewById(R.id.tv_ticketInfo);


        tv_ticketInfo.setText(Arrays.toString(list.toArray()));

        showTables = new ArrayList<>();
        allPassTables = new ArrayList<>();


        showTables = dbaccess.getShowDates();



        allPassTables = dbaccess.getAllPass();

        currentShowDate = 0;
        initFragment(4);


    }


    private void setupShowRGGroups() {

        Log.e("Modernsky", "Setting up show groups");



    }


    private void initFragment(int i) {
        // TODO Auto-generated method stub
        // 由于是引用了V4包下的Fragment，所以这里的管理器要用getSupportFragmentManager获取
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 开启事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 隐藏所有Fragment
        hideFragment(transaction);
        switch (i) {
            case 0:
                tv_title.setText("扫身份证");
                if (idcardFragment == null) {
                    idcardFragment = new IdcardFragment();
                    transaction.add(R.id.fl_content, idcardFragment);
                } else {
                    transaction.show(idcardFragment);
                }
                break;
            case 1:
                tv_title.setText("手工输入身份证信息");
                if (idcardFragment2 == null) {
                    idcardFragment2 = new IdcardFragment2();
                    transaction.add(R.id.fl_content, idcardFragment2);
                } else {
                    transaction.show(idcardFragment2);
                }
                break;
            case 2:
                tv_title.setText("指纹");
                if (fpFragment == null) {
                    fpFragment = new FpFragment();
                    transaction.add(R.id.fl_content, fpFragment);
                } else {
                    transaction.show(fpFragment);
                }
                break;
            case 3:
                tv_title.setText("人脸");
                if (faceFragment == null) {
                    faceFragment = new FaceFragment();
                    transaction.add(R.id.fl_content, faceFragment);
                } else {
                    transaction.show(faceFragment);
                }
                break;
            case 4:
                tv_title.setText("数据库");
                if (dbFragment == null) {
                    dbFragment = new DbFragment();

                    Bundle args = new Bundle();
                    ArrayList<String> tables = new ArrayList<>();

                    for(int ct =0; ct< showTables.size(); ct++) {

                        tables.add(showTables.get(ct).toString());
                    }

                    args.putStringArrayList(DbFragment.ARG_TAG, tables);
                    dbFragment.setArguments(args);


                    transaction.add(R.id.fl_content, dbFragment);
                } else {
                    transaction.show(dbFragment);
                }

                //setupShowRGGroups();
                break;
            case 5:
                tv_title.setText("黑名单");
                if (blackFragment == null) {
                    blackFragment = new BlackDbFragment();
                    transaction.add(R.id.fl_content, blackFragment);
                } else {
                    transaction.show(blackFragment);
                }
                break;
            case 6:
                tv_title.setText("关于");
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                    transaction.add(R.id.fl_content, aboutFragment);
                }else {
                    transaction.show(aboutFragment);
                }
            default:
                break;
        }
        // 提交事务
        transaction.commit();
    }

    // 隐藏Fragment
    private void hideFragment(FragmentTransaction transaction) {
        if (idcardFragment != null) {
            transaction.hide(idcardFragment);
        }
        if (idcardFragment2 != null) {
            transaction.hide(idcardFragment2);
        }
        if (fpFragment != null) {
            transaction.hide(fpFragment);
        }
        if (faceFragment != null) {
            transaction.hide(faceFragment);
        }
        if (dbFragment != null) {
            transaction.hide(dbFragment);
        }
        if (blackFragment != null) {
            transaction.hide(blackFragment);
        }
        if (aboutFragment != null) {
            transaction.hide(aboutFragment);
        }
    }

    private void initView() {
        // TODO Auto-generated method stub
        setContentView(R.layout.activity_main);
        ll_idcard = (LinearLayout) findViewById(R.id.ll_idcard);
        ll_idcard2 = (LinearLayout) findViewById(R.id.ll_idcard2);

        ll_db = (LinearLayout) findViewById(R.id.ll_db);
        //ll_idcard2.setVisibility(View.GONE);


        ll_fp = (LinearLayout) findViewById(R.id.ll_fp);
        ll_fp.setVisibility(View.GONE);
        ll_face = (LinearLayout) findViewById(R.id.ll_face);
        ll_face.setVisibility(View.GONE);

        ll_black = (LinearLayout) findViewById(R.id.ll_black_db);
        ll_black.setVisibility(View.GONE);


        ll_about = (LinearLayout) findViewById(R.id.ll_about);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    private void initEvent() {
        // TODO Auto-generated method stub
        ll_idcard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(0);
            }
        });
        ll_idcard2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(1);
            }
        });
        ll_fp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(2);
            }
        });
        ll_face.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(3);
            }
        });
        ll_db.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(4);
            }
        });
        ll_black.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(5);
            }
        });
        ll_about.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                initFragment(6);
            }
        });
    }


    private void processId(String id_info) {

        String opmode = dbaccess.getOperationMode();
        TextView tv_ticketInfo = (TextView) findViewById(R.id.tv_ticketInfo);
        String msg = "";

        if(opmode.equals("sqlite")) {



            List<TicketInfo> avail_tickets = new ArrayList<>();

            if (showTables.size() > currentShowDate) {
                avail_tickets = dbaccess.findTicket(showTables.get(currentShowDate).getTableName(), id_info);
            }


            if (allPassTables.size() > 0) {

                for (int i = 0; i < allPassTables.size(); i++) {

                    List<TicketInfo> tmp_list = dbaccess.findTicket(allPassTables.get(i).getTableName(), id_info);
                    avail_tickets.addAll(tmp_list);
                }
            }


            if (avail_tickets.size() == 0) {

                tv_ticketInfo.setText("没有票");
                tv_ticketInfo.setBackgroundColor(Color.RED);
                return;
            }


            msg = "找到了 " + avail_tickets.size() + " 张票\n-------------\n";
            for (int i = 0; i < avail_tickets.size(); i++) {

                msg += avail_tickets.get(i).toString();
                msg += "\n";


            }

            List<String> check_ins = new ArrayList<>();

            check_ins = dbaccess.findCheckIn(showTables.get(currentShowDate).getDate(), id_info);

            //msg += "Found " + check_ins.size() + " check_ins";


            if (avail_tickets.size() > check_ins.size()) {

                for (int ct = 0; ct < (avail_tickets.size() - check_ins.size()); ct++) {


                    dbaccess.doCheckIn(id_info, showTables.get(currentShowDate).getDate(),"已取票");
                }

                msg += "\n兑票完成";
                tv_ticketInfo.setBackgroundColor(Color.GREEN);

            } else {

                msg += "\n都已经入场！！！\n--------------------\n";

                for (int ct = 0; ct < check_ins.size(); ct++) {

                    msg += "\n--" + check_ins.get(ct).toString();
                }

                tv_ticketInfo.setBackgroundColor(Color.YELLOW);

            }

        } else if(opmode.equals("mssql")) {


            int avail_tickets = dbaccess.doMsSqlFindAvailTickets(id_info, showTables.get(currentShowDate).getDay());


            if(avail_tickets<0) {
                tv_ticketInfo.setText("没有票");
                tv_ticketInfo.setBackgroundColor(Color.RED);
                return;

            } else if (avail_tickets == 0) {

                msg = "\n都已经入场！！！\n--------------------\n";

                List<String> ticket_info = dbaccess.doMsSqlFindTickets(id_info, showTables.get(currentShowDate).getDay());
                for(int ct=0; ct< ticket_info.size(); ct++) {

                    msg += "\n--" + ticket_info.get(ct).toString();

                }

                List<String> list = dbaccess.doMsSqlCheckin(id_info, showTables.get(currentShowDate).getDate(), 1, 0);
                for(int ct=0; ct<list.size(); ct++) {

                    msg += "\n--" + list.get(ct).toString();
                }

                tv_ticketInfo.setBackgroundColor(Color.YELLOW);

            } else {

                msg = "\n兑票完成";

                List<String> ticket_info = dbaccess.doMsSqlFindTickets(id_info, showTables.get(currentShowDate).getDay());
                for(int ct=0; ct< ticket_info.size(); ct++) {

                    msg += "\n--" + ticket_info.get(ct).toString();

                }

                List<String> list = dbaccess.doMsSqlCheckin(id_info, showTables.get(currentShowDate).getDate(), avail_tickets, 1);

                for(int ct=0; ct<list.size(); ct++) {

                    msg += "\n--" + list.get(ct).toString();
                }
                tv_ticketInfo.setBackgroundColor(Color.GREEN);
            }


        }
        tv_ticketInfo.setText(msg);

    }
    @Override
    public void onGetID(String id_info) {


        processId(id_info);



    }

    @Override
    public void onShowSelectChange(int id) {
        currentShowDate = id;

        Toast.makeText(this, "验票日期" + showTables.get(currentShowDate).toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onManualSearchID(String id_info) {


        processId(id_info);

    }
}
