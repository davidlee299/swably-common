package goofy2.swably;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.TextView;
import goofy2.swably.fragment.AppAboutFragment;
import goofy2.swably.fragment.AppCommentsFragment;
import goofy2.swably.fragment.UserReviewsFragment;
import goofy2.swably.fragment.App.RefreshAppBroadcastReceiver;

public class App extends WithHeaderActivity
	implements AppCommentsFragment.OnAboutListener 
{
	String mId = null;
	int currentMenu;
	protected AppHeader header = new AppHeader(this);
	protected AppActionHelper actionHelper = new AppActionHelper(this, header);  
	protected RefreshAppBroadcastReceiver mRefreshAppReceiver = new RefreshAppBroadcastReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        mId = getIdFromUrl(i);
    	if(mId != null){
    		JSONObject json = new JSONObject();
    		try {
				json.put("id", mId);
	    		String str = loadCache(AppProfile.cacheId(mId));
	    		if(str != null){
	    			i.putExtra(Const.KEY_APP, str);
	    		}else{
		    		i.putExtra(Const.KEY_APP, json.toString());
	    		}
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
        super.onCreate(savedInstanceState);
    	enableSlidingMenu();
        setContentView(R.layout.app);
        header.setAppFromIntent();
        header.setAppFromCache(header.getAppId());
//        if(header.getApp().getPackage() != null) header.setAppFromDb(header.getApp());
		registerReceiver(mRefreshAppReceiver, new IntentFilter(Const.BROADCAST_REFRESH_APP));
    }

    private String getIdFromUrl(Intent intent){
    	String ret = null;
    	Uri data = intent.getData();
    	if(data != null){
	    	List<String> params = data.getPathSegments();
	    	//String action = params.get(0); // "a"
	    	if("http".equalsIgnoreCase(data.getScheme())) ret = params.get(1);
    	}
    	return ret;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
		actionHelper.init(findViewById(R.id.body));
		bind();
		addReviewsFragment();		
    }

    protected void addReviewsFragment(){
		Bundle bundle = new Bundle();
		bundle.putString(Const.KEY_APP, header.getApp().getJSON().toString());

		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();	
		AppCommentsFragment fragment = new AppCommentsFragment();
		fragment.setArguments(bundle);
		ft.add(R.id.fragment, fragment);
		ft.commit();

		currentMenu = R.menu.app_reviews;
    }
    
    protected void addAboutFragment(){
		Bundle bundle = new Bundle();
		bundle.putString(Const.KEY_APP, header.getApp().getJSON().toString());

		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();	
		AppAboutFragment fragment = new AppAboutFragment();
		fragment.setArguments(bundle);
		ft.add(R.id.fragment, fragment);
//		ft.addToBackStack(null);
		ft.commit();
		
		currentMenu = R.menu.app_about;
    }

    @Override
    public void onDestroy(){
		unregisterReceiver(mRefreshAppReceiver);
    	super.onDestroy();
    }

    public void bind() {
//		TextView tv = (TextView)findViewById(R.id.txtTitle);
//		tv.setText(header.getApp().getName());
    	actionHelper.bind();
	}

    public void onAbout(){
    	addAboutFragment();
    }
    
    protected int getMenu(){
    	return R.menu.app_reviews;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == R.id.about) {
			startActivity(new Intent(this, AppAbout.class).putExtra(Const.KEY_APP, header.getApp().getJSON().toString()));
	    	return true;
//	    }else if (item.getItemId() == R.id.share) {
//			sendOutApp(header.getApp());
//	    	return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}

	public class RefreshAppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.BROADCAST_REFRESH_APP)){
            	String pkg = intent.getStringExtra(Const.KEY_PACKAGE);
            	if(pkg != null && pkg.equalsIgnoreCase(header.getApp().getPackage())){
        			AppHelper helper = new AppHelper(App.this);
        			goofy2.swably.data.App app = helper.getApp(pkg);
            		if(app != null) header.setApp(app);
            		bind();
            	}else{
                	String id = intent.getStringExtra(Const.KEY_ID);
            		if(id != null && id.equals(header.getApp().getCloudId())){
            			String str = loadCache(AppProfile.cacheId(id));
            			if(str != null){
            				try {
								header.setApp(new goofy2.swably.data.App(new JSONObject(str)));
								bind();
							} catch (JSONException e) {
								e.printStackTrace();
							}
            			}
            		}
            	}
            }
        }
    }
}
