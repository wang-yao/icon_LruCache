package com.example.icon;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageAdapter extends BaseAdapter {
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20;

	Context c;
	List<String> list;
	LayoutInflater inflater;
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskCache;

	@SuppressLint("NewApi")
	private void init(Context context) {
		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		memClass = memClass > 32 ? 32 : memClass;
		int cacheSize = 1024 * 1024 * memClass / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cache_icon";
		mDiskCache = DiskLruCache.openCache(context, new File(path), DISK_CACHE_SIZE);

	}

	public ImageAdapter(Context c, List<String> list) {
		this.c = c;
		this.list = list;
		inflater = LayoutInflater.from(c);
		init(c);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SurfaceHolder surfaceHolder = new SurfaceHolder();
		if (convertView != null) {
			surfaceHolder = (SurfaceHolder) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.image_item, null);
			surfaceHolder.iv = (ImageView) convertView.findViewById(R.id.imageView1);
		}
		convertView.setTag(surfaceHolder);
		String url = getItem(position);
		if (cancelPotentialLoad(url, surfaceHolder.iv)) {
			AsyncLoadImageTask t = new AsyncLoadImageTask(surfaceHolder.iv);
			surfaceHolder.iv.setImageDrawable(new LoadingDrawable(t));
			t.execute(url);
		}

		return convertView;
	}

	private boolean cancelPotentialLoad(String url, ImageView imageview) {

		AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
		if (loadImageTask != null) {
			String bitmapUrl = loadImageTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				loadImageTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	private AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview) {
		if (imageview != null) {
			Drawable drawable = imageview.getDrawable();
			if (drawable instanceof LoadingDrawable) {
				LoadingDrawable loadedDrawable = (LoadingDrawable) drawable;
				return loadedDrawable.getLoadImageTask();
			}
		}
		return null;
	}

	static class SurfaceHolder {
		ImageView iv;
	}

	class AsyncLoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;
		private String url = null;

		public AsyncLoadImageTask(ImageView imageview) {
			super();
			imageViewReference = new WeakReference<ImageView>(imageview);
		}

		@SuppressLint("NewApi")
		@Override
		protected Bitmap doInBackground(String... params) {

			Bitmap bitmap = null;
			this.url = params[0];

			bitmap = mMemoryCache.get(url);
			if (bitmap != null) {
				return bitmap;
			}
			bitmap = mDiskCache.get(url);
			if (bitmap != null) {
				mMemoryCache.put(url, bitmap);
				return bitmap;
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(url, opts);
			opts.inSampleSize = Math.max((int) (opts.outHeight / (float) 100), (int) (opts.outWidth / (float) 100));
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(url, opts);
			if (url != null && bitmap != null) {
				mMemoryCache.put(url, bitmap);
				mDiskCache.put(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap resultBitmap) {
			super.onPostExecute(resultBitmap);
			ImageView i = imageViewReference.get();
			i.setImageBitmap(resultBitmap);
		}
	}

	public static class LoadingDrawable extends ColorDrawable {
		private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

		public LoadingDrawable(AsyncLoadImageTask loadImageTask) {
			super(Color.GRAY);
			loadImageTaskReference = new WeakReference<AsyncLoadImageTask>(loadImageTask);
		}

		public AsyncLoadImageTask getLoadImageTask() {
			return loadImageTaskReference.get();
		}
	}
}
