package com.icanstudioz.taxi.fragement;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.Server.Server;
import com.icanstudioz.taxi.acitivities.HomeActivity;
import com.icanstudioz.taxi.custom.Utils;
import com.icanstudioz.taxi.pojo.User;
import com.icanstudioz.taxi.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;
import gun0912.tedbottompicker.TedBottomPicker;

/**
 * Created by android on 8/4/17.
 */
public class UploadDomentFragment extends FragmentManagePermission {
    View view;
    SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageview_licence;
    private ImageView imageview_insurace;
    private ImageView imageview_permit;
    private ImageView imageview_registration;
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE};
    private File imageFile;
    ProgressBar progressBar_licence, progressBar_insurance, progressBar_permit, ProgressBar_registration;
    ImageView img_licence, img_insurance, img_permit, img_registration;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_document, container, false);
        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.upload_doc));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BindView();
    }

    public void setofflineDoc(String doc, String url) {
        User user = SessionManager.getUser();
        Gson gson = new Gson();

        if (doc.equalsIgnoreCase("l")) {
            user.setLicence(url);
        } else if (doc.equalsIgnoreCase("i")) {
            user.setInsurance(url);
        } else if (doc.equalsIgnoreCase("p")) {
            user.setPermit(url);
        } else if (doc.equalsIgnoreCase("r")) {
            user.setRegisteration(url);
        } else {
            //Nothing
        }
        SessionManager.setUser(gson.toJson(user));

    }

    public void BindView() {
        imageview_licence = (ImageView) view.findViewById(R.id.imageview_licence);
        imageview_insurace = (ImageView) view.findViewById(R.id.imageview_insurance);
        imageview_permit = (ImageView) view.findViewById(R.id.imageview_permit);
        imageview_registration = (ImageView) view.findViewById(R.id.imageview_registration);
        CardView card_licence = (CardView) view.findViewById(R.id.card_licence);
        CardView card_insurance = (CardView) view.findViewById(R.id.card_insurance);
        CardView card_permit = (CardView) view.findViewById(R.id.card_permit);
        CardView card_registratiom = (CardView) view.findViewById(R.id.card_registration);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        progressBar_licence = (ProgressBar) view.findViewById(R.id.progressbar_licence);
        progressBar_insurance = (ProgressBar) view.findViewById(R.id.progressbar_insurance);
        progressBar_permit = (ProgressBar) view.findViewById(R.id.progressbar_permit);
        ProgressBar_registration = (ProgressBar) view.findViewById(R.id.progressbar_registration);
        img_licence = (ImageView) view.findViewById(R.id.image_licence);
        img_insurance = (ImageView) view.findViewById(R.id.image_insurance);
        img_permit = (ImageView) view.findViewById(R.id.image_permit);
        img_registration = (ImageView) view.findViewById(R.id.image_registration);
        overrideFonts(getActivity(), view);
        if (Utils.haveNetworkConnection(getActivity())) {
            getInfo();
        } else {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        card_licence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getActivity())) {
                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
                            TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                        @Override
                                        public void onImageSelected(Uri uri) {
                                            // here is selected uri
                                            imageFile = new File(uri.getPath());
                                            String format = getMimeType(getActivity(), uri);
                                            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") || format.equalsIgnoreCase("gif") || format.equalsIgnoreCase("jpeg")) {
                                                upload_pic("l", format);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.format_msg), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                            Log.d(getTag(), message);
                                        }
                                    })
                                    .create();

                            tedBottomPicker.show(getActivity().getSupportFragmentManager());
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

                }


            }
        });
        card_insurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getActivity())) {
                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
                            TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                        @Override
                                        public void onImageSelected(Uri uri) {
                                            // here is selected uri
                                            imageFile = new File(uri.getPath());
                                            String format = getMimeType(getActivity(), uri);
                                            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") || format.equalsIgnoreCase("gif")) {
                                                upload_pic("i", format);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.format_msg), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                            Log.d(getTag(), message);
                                        }
                                    })
                                    .create();

                            tedBottomPicker.show(getActivity().getSupportFragmentManager());
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

                }

            }
        });
        card_permit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getActivity())) {
                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
                            TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                        @Override
                                        public void onImageSelected(Uri uri) {
                                            // here is selected uri
                                            imageFile = new File(uri.getPath());

                                            String format = getMimeType(getActivity(), uri);
                                            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") || format.equalsIgnoreCase("gif")) {
                                                upload_pic("p", format);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.format_msg), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                            Log.d(getTag(), message);
                                        }
                                    })
                                    .create();

                            tedBottomPicker.show(getActivity().getSupportFragmentManager());
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();

                }

            }
        });
        card_registratiom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.haveNetworkConnection(getActivity())) {
                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
                            TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                        @Override
                                        public void onImageSelected(Uri uri) {
                                            // here is selected uri
                                            imageFile = new File(uri.getPath());
                                            //  profile_pic.setImageURI(uri);
                                            String format = getMimeType(getActivity(), uri);
                                            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") || format.equalsIgnoreCase("gif")) {
                                                upload_pic("r", format);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.format_msg), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(getActivity(), getString(R.string.tryagian), Toast.LENGTH_LONG).show();
                                            Log.d(getTag(), message);
                                        }
                                    })
                                    .create();

                            tedBottomPicker.show(getActivity().getSupportFragmentManager());
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void getInfo() {
        RequestParams params = new RequestParams();
        params.put("user_id", SessionManager.getUserId());

        User user = SessionManager.getUser();
        img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
        Glide.with(getActivity()).load(user.getLicence()).into(imageview_licence);

        img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
        Glide.with(getActivity()).load(user.getInsurance()).into(imageview_insurace);

        img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
        Glide.with(getActivity()).load(user.getPermit()).into(imageview_permit);

        img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
        Glide.with(getActivity()).load(user.getRegisteration()).into(imageview_registration);


        Server.setHeader(user.getKey());
        Server.get(Server.GET_PROFILE, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                       if (user.getLicence() != null) {
                            img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
                            Glide.with(UploadDomentFragment.this).load(user.getLicence()).into(imageview_licence);
                        } else {
                            img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        }
                        if (user.getInsurance() != null) {
                            img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
                            Glide.with(getActivity()).load(user.getInsurance()).into(imageview_insurace);
                        } else {
                            img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        }
                        if (user.getPermit() != null) {
                            img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
                            Glide.with(getActivity()).load(user.getPermit()).into(imageview_permit);
                        } else {
                            img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        }
                        if (user.getRegisteration() != null) {
                            img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
                            Glide.with(getActivity()).load(user.getRegisteration()).into(imageview_registration);
                        } else {
                            img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        }


                    } else {

                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {

                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }


        });

    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    private void upload_pic(final String DocName, String type) {
        RequestParams params = new RequestParams();
        if (DocName.equalsIgnoreCase("l")) {
            progressBar_licence.setVisibility(View.VISIBLE);
            if (imageFile != null) {
                try {

                    if (type.equals("jpg")) {
                        params.put("license", imageFile, "image/jpeg");
                    } else if (type.equals("jpeg")) {
                        params.put("license", imageFile, "image/jpeg");
                    } else if (type.equals("png")) {
                        params.put("license", imageFile, "image/png");
                    } else {
                        params.put("license", imageFile, "image/gif");
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (DocName.equalsIgnoreCase("i")) {
            progressBar_insurance.setVisibility(View.VISIBLE);
            if (imageFile != null) {
                try {

                    if (type.equals("jpg")) {
                        params.put("insurance", imageFile, "image/jpeg");
                    } else if (type.equals("jpeg")) {
                        params.put("insurance", imageFile, "image/jpeg");
                    } else if (type.equals("png")) {
                        params.put("insurance", imageFile, "image/png");
                    } else {
                        params.put("insurance", imageFile, "image/gif");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (DocName.equalsIgnoreCase("p")) {
            progressBar_permit.setVisibility(View.VISIBLE);
            if (imageFile != null) {
                try {

                    if (type.equals("jpg")) {
                        params.put("permit", imageFile, "image/jpeg");
                    } else if (type.equals("jpeg")) {
                        params.put("permit", imageFile, "image/jpeg");
                    } else if (type.equals("png")) {
                        params.put("permit", imageFile, "image/png");
                    } else {
                        params.put("permit", imageFile, "image/gif");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (DocName.equalsIgnoreCase("r")) {
            ProgressBar_registration.setVisibility(View.VISIBLE);
            if (imageFile != null) {
                try {

                    if (type.equals("jpg")) {
                        params.put("registration", imageFile, "image/jpeg");
                    } else if (type.equals("jpeg")) {
                        params.put("registration", imageFile, "image/jpeg");
                    } else if (type.equals("png")) {
                        params.put("registration", imageFile, "image/png");
                    } else {
                        params.put("registration", imageFile, "image/gif");
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }


        Server.setHeader(SessionManager.getKEY());
        params.put("user_id", SessionManager.getUserId());
        Server.post(Server.UPDATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        if (DocName.equalsIgnoreCase("l")) {
                            setofflineDoc(DocName, response.getJSONObject("data").getString("license"));
                            setVisibility(DocName, "success", response.getJSONObject("data").getString("license"));
                        } else if (DocName.equalsIgnoreCase("i")) {
                            setofflineDoc(DocName, response.getJSONObject("data").getString("insurance"));
                            setVisibility(DocName, "success", response.getJSONObject("data").getString("insurance"));
                        } else if (DocName.equalsIgnoreCase("p")) {
                            setofflineDoc(DocName, response.getJSONObject("data").getString("permit"));
                            setVisibility(DocName, "success", response.getJSONObject("data").getString("permit"));
                        } else if (DocName.equalsIgnoreCase("r")) {
                            setofflineDoc(DocName, response.getJSONObject("data").getString("registration"));
                            setVisibility(DocName, "success", response.getJSONObject("data").getString("registration"));
                        }
                    } else {
                        Toast.makeText(getActivity(), response.getString("data"), Toast.LENGTH_LONG).show();
                        setVisibility(DocName, "fail", "");
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                    setVisibility(DocName, "fail", "");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                // Log.e("fialure",responseString+"  "+throwable.toString());
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                setVisibility(DocName, "fail", "");
            }
        });

    }


  /*  public void getVehicleInfo() {
        Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();


        String licence = user.get(SessionManager.DRivingLicence);
        String insurance = user.get(SessionManager.VehicleInsurance);
        String permit = user.get(SessionManager.VehiclePermit);
        String registration = user.get(SessionManager.VehicleRegistartion);

        if (licence != null && !licence.equals("")) {
            img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            Glide.with(getActivity()).load(licence).into(imageview_licence);
        } else {
            img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
        }
        if (insurance != null && !insurance.equals("")) {
            img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            Glide.with(getActivity()).load(insurance).into(imageview_insurace);
        } else {
            img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
        }
        if (permit != null && !permit.equals("")) {
            img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            Glide.with(getActivity()).load(permit).into(imageview_permit);
        } else {
            img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
        }
        if (registration != null && !registration.equals("")) {
            img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            Glide.with(getActivity()).load(registration).into(imageview_registration);
        } else {
            img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));

        }
    }*/

    public void setVisibility(String DocName, String status, String url) {
        if (DocName.equalsIgnoreCase("l")) {

            progressBar_licence.setVisibility(View.GONE);
            if (status.equalsIgnoreCase("success")) {
                Glide.with(getActivity()).load(url).into(imageview_licence);
                img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            } else {
                img_licence.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
            }
        } else if (DocName.equalsIgnoreCase("i")) {

            progressBar_insurance.setVisibility(View.GONE);
            if (status.equalsIgnoreCase("success")) {
                Glide.with(getActivity()).load(url).into(imageview_insurace);
                img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));

            } else {
                img_insurance.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
            }
        } else if (DocName.equalsIgnoreCase("p")) {

            progressBar_permit.setVisibility(View.GONE);
            if (status.equalsIgnoreCase("success")) {
                Glide.with(getActivity()).load(url).into(imageview_permit);
                img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            } else {
                img_permit.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
            }
        } else if (DocName.equalsIgnoreCase("r")) {

            ProgressBar_registration.setVisibility(View.GONE);
            if (status.equalsIgnoreCase("success")) {
                Glide.with(getActivity()).load(url).into(imageview_registration);
                img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green));
            } else {
                img_registration.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
            }
        }


    }

    private void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof AppCompatButton) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            } else if (v instanceof EditText) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            }

        } catch (Exception e) {

        }
    }
}
