package fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

	protected Activity context;

	/**
	 * initialize view from first create
	 * 
	 * @param view
	 */
	abstract protected void init(View view);

	/**
	 * @return resource id of layout of fragment
	 */
	abstract protected int getLayoutId();

	/**
	 * Function only called when fragment is created when configuration changed
	 * 
	 * @param savedInstanceState
	 */
	protected void onOrientationChanged(Bundle savedInstanceState) {
		// Call when orientation changed
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			onOrientationChanged(savedInstanceState);
		}
		View view = inflater.inflate(getLayoutId(), container, false);
		init(view);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity;
	}

}
