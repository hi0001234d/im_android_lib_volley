/**
 * 
 */
package com.android.volley.toolbox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

public class MultiPartRequest<T> extends Request<T>
{

	private static String FILE_PART_NAME = "file";

	private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
	private final Response.Listener<T> mListener;
	private final File mImageFile;
	protected Map<String, String> headers;

	public MultiPartRequest( String url, ErrorListener errorListener, Listener<T> listener, String file_field_name, File imageFile )
	{
		super(Method.POST, url, errorListener);

		FILE_PART_NAME = file_field_name; 
		mListener = listener;
		mImageFile = imageFile;
		buildMultipartEntity();
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		Map<String, String> headers = super.getHeaders();

		if ( headers == null || headers.equals(Collections.emptyMap()) )
		{
			headers = new HashMap<String, String>();
		}
		headers.put("Accept", "application/json");
		return headers;
	}

	private void buildMultipartEntity()
	{
		mBuilder.addBinaryBody(FILE_PART_NAME, mImageFile, ContentType.create("image/jpeg"), mImageFile.getName());
		mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
	}

	@Override
	public String getBodyContentType()
	{
		String contentTypeHeader = mBuilder.build().getContentType().getValue();
		return contentTypeHeader;
	}

	@Override
	public byte[] getBody() throws AuthFailureError
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			mBuilder.build().writeTo(bos);
		}
		catch ( IOException e )
		{
			VolleyLog.e( "IOException writing to ByteArrayOutputStream bos, building the multipart request. " );
			Log.e( "im_android_lib", "exception", e);
		}
		return bos.toByteArray(); 
	}

	@Override
	protected Response<T> parseNetworkResponse( NetworkResponse response )
	{
		T result = null;
		return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse( T response )
	{
		mListener.onResponse(response);
	}
}