package cn.crepusculo.subway_ticket_android.ui.activity.login;

import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dd.processbutton.iml.ActionProcessButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.subwayticket.model.request.LoginRequest;
import com.subwayticket.model.request.PhoneCaptchaRequest;
import com.subwayticket.model.request.RegisterRequest;
import com.subwayticket.model.request.ResetPasswordRequest;
import com.subwayticket.model.result.MobileLoginResult;
import com.subwayticket.model.result.Result;

import cn.crepusculo.subway_ticket_android.R;
import cn.crepusculo.subway_ticket_android.preferences.Info;
import cn.crepusculo.subway_ticket_android.ui.activity.BaseActivity;
import cn.crepusculo.subway_ticket_android.util.GsonUtils;
import cn.crepusculo.subway_ticket_android.util.NetworkUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    ViewGroup.LayoutParams buttonSize;
    // Default method login
    private String mode = Mode.LOGIN;
    private LinearLayout card;
    private MaterialEditText editTextUserName;
    private MaterialEditText editTextPassword;
    private MaterialEditText editTextCaptcha;
    private ActionProcessButton loginBtn;
    private Button signBtn;
    private Button forgetBtn;
    private TextSwitcher textSwitcher;
    private TextSwitcher textSwitcher2;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        initCard();
        initHint();

        // record size of button
        buttonSize = new ViewGroup.LayoutParams(
                forgetBtn.getLayoutParams().width,
                forgetBtn.getLayoutParams().height);
    }

    /**
     * Hint Title
     * <p/>
     * Each time mode switch, the hint title will switch also.
     */
    private void initHint() {
        textSwitcher = (TextSwitcher) findViewById(R.id.hint);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
                TextView textView = (TextView) inflater.inflate(R.layout.item_hint, null);
                textView.setPadding(40, 70, 0, 0);

                return textView;
            }
        });
        textSwitcher.setInAnimation(this, R.anim.fade_in_center);
        textSwitcher.setOutAnimation(this, R.anim.fade_out_center);

        textSwitcher2 = (TextSwitcher) findViewById(R.id.hint_bigger);
        textSwitcher2.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
                TextView textView = (TextView) inflater.inflate(R.layout.item_hint_bigger, null);
                textView.setPadding(40, 100, 0, 0);
                return textView;
            }
        });
        textSwitcher2.setInAnimation(this, R.anim.fade_in_center);
        textSwitcher2.setOutAnimation(this, R.anim.fade_out_center);

        textSwitcher.setText(getResources().getString(R.string.login_backup));
        textSwitcher2.setText(getResources().getString(R.string.login_backup_e));
    }


    /**
     * init login card view
     */
    private void initCard() {
        card = (LinearLayout) findViewById(R.id.card);

        editTextUserName = (MaterialEditText) findViewById(R.id.edit_text_id);
        editTextPassword = (MaterialEditText) findViewById(R.id.edit_text_pwd);

        editTextCaptcha = (MaterialEditText) findViewById(R.id.edit_text_captcha);

        signBtn = (Button) findViewById(R.id.login_signup);
        forgetBtn = (Button) findViewById(R.id.login_forget);
        loginBtn = (ActionProcessButton) findViewById(R.id.login_login);

        loginBtn.setOnClickListener(this);

        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSubmitTitle(Mode.REGISTER_CAPTCHA);

            }
        });
        forgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSubmitTitle(Mode.UPDATE_CAPTCHA);
            }
        });

        // --------- Fill with cache ------------
        editTextUserName.setText(Info.getInstance().user.getId());
    }

    @Override
    public void onClick(View view) {
        loginBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        // to progress mode
        loginBtn.setProgress(32);

        final String loginId = editTextUserName.getText().toString().trim();
        final String loginPwd = editTextPassword.getText().toString().trim();
        /**
         * Mode.LOGIN
         * Use to login
         */
        if (mode.equals(Mode.LOGIN)) {
            editTextPassword.setHint(R.string.login_user_password);
            login(loginId, loginPwd);
        } // END IF

        /**
         * Mode.REGISTER
         * Use to Register a new account
         * Step 1. get Captcha
         * Step 2. register
         *
         */
        else if (mode.equals(Mode.REGISTER_CAPTCHA)) {
            editTextPassword.setHint(R.string.login_user_password);

            NetworkUtils.accountGetCaptcha(
                    new PhoneCaptchaRequest(editTextUserName.getText().toString().trim()),
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            Log.e("Register GetCaptcha", "Success!" + response.getResultCode());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginBtn.setProgress(100);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Update mode
                                            loginBtn.setProgress(0);
                                            mode = Mode.REGISTER;
                                            setSubmitTitle(mode);
                                        }
                                    }, 1500);
                                }
                            }, 1500);
                            /**
                             * IF AND ONLY IF getCaptcha successful
                             *
                             */
                            showView(editTextCaptcha);
                            hideView(signBtn);
                            hideView(forgetBtn);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                                Snackbar.make(findViewById(R.id.login_activity), r.result_description, Snackbar.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update mode
                                        loginBtn.setProgress(-1);
                                    }
                                }, 1500);
                            } catch (NullPointerException e) {
                                Snackbar.make(findViewById(R.id.login_activity), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                            } finally {
                                loginBtn.setProgress(-1);
                            }
                        }
                    });
        } else if (mode.equals(Mode.REGISTER)) {
            NetworkUtils.accountRegister(new RegisterRequest(
                            editTextUserName.getText().toString().trim(),
                            editTextPassword.getText().toString().trim(),
                            editTextCaptcha.getText().toString().trim()),
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            loginBtn.setProgress(100);
                            setSubmitTitle(Mode.LOGIN);
                            login(editTextUserName.getText().toString().trim(),
                                    editTextPassword.getText().toString().trim());
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                                Snackbar.make(findViewById(R.id.login_activity), r.result_description, Snackbar.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update mode
                                        loginBtn.setProgress(-1);
                                    }
                                }, 1500);
                            } catch (NullPointerException e) {
                                Snackbar.make(findViewById(R.id.login_activity), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                            } finally {
                                loginBtn.setProgress(-1);
                            }
                        }
                    }
            );
        }
        /**
         * Mode.Update
         * Use to update password by captcha
         * Step 1. get Captcha
         * Step 2. update new password
         *
         */
        else if (mode.equals(Mode.UPDATE_CAPTCHA)) {
            editTextPassword.setText(null);
            editTextPassword.setHint(R.string.login_user_password_update);
            NetworkUtils.accountGetCaptcha(
                    new PhoneCaptchaRequest(editTextUserName.getText().toString().trim()),
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            Log.e("Update GetCaptcha", "Success!" + response.getResultCode());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginBtn.setProgress(100);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Update mode
                                            loginBtn.setProgress(0);
                                            mode = Mode.UPDATE;
                                            setSubmitTitle(mode);
                                        }
                                    }, 1500);
                                }
                            }, 1500);
                            /**
                             * IF AND ONLY IF getCaptcha successful
                             *
                             */
                            showView(editTextCaptcha);
                            showView(editTextPassword);
                            hideView(signBtn);
                            hideView(forgetBtn);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                                Snackbar.make(findViewById(R.id.login_activity), r.result_description, Snackbar.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update mode
                                        loginBtn.setProgress(-1);
                                    }
                                }, 1500);
                            } catch (NullPointerException e) {
                                Snackbar.make(findViewById(R.id.login_activity), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                            } finally {
                                loginBtn.setProgress(-1);
                            }
                        }
                    });
        } else if (mode.equals(Mode.UPDATE)) {
            NetworkUtils.accountResetPassword(
                    new ResetPasswordRequest(
                            editTextUserName.getText().toString().trim(),
                            editTextPassword.getText().toString().trim(),
                            editTextCaptcha.getText().toString().trim()),
                    new Response.Listener<Result>() {
                        @Override
                        public void onResponse(Result response) {
                            loginBtn.setProgress(100);
                            setSubmitTitle(Mode.LOGIN);
                            login(editTextUserName.getText().toString().trim(),
                                    editTextPassword.getText().toString().trim());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                                Snackbar.make(findViewById(R.id.login_activity), r.result_description, Snackbar.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update mode
                                        loginBtn.setProgress(-1);
                                    }
                                }, 1500);
                            } catch (NullPointerException e) {
                                Snackbar.make(findViewById(R.id.login_activity), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                            } finally {
                                loginBtn.setProgress(-1);
                            }
                        }
                    });

        }
    }

    /**
     * Reset LoginBtn Title when mode change
     * Call by subSubmitTitle@override
     */
    private void setSubmitTitle() {
        Log.e("Set", "submitTitle");
        if (mode.equals(Mode.LOGIN)) {
            loginBtn.setText(R.string.login_submit);
        } else if (mode.equals(Mode.REGISTER_CAPTCHA)) {
            loginBtn.setText(R.string.login_captcha);
        } else if (mode.equals(Mode.UPDATE)) {
            loginBtn.setText(R.string.login_update);
        } else if (mode.equals(Mode.UPDATE_CAPTCHA)) {
            loginBtn.setText(R.string.login_captcha);
        } else {
            loginBtn.setText(R.string.login_signup);
        }
    }

    private void setSubmitTitle(String mode) {
        this.mode = mode;
        loginBtn.setProgress(0);
        setSubmitTitle();

        /**
         * == START IF == register_captcha ==
         */
        if (mode.equals(Mode.REGISTER_CAPTCHA)) {
            textSwitcher.setText(getResources().getString(R.string.login_signup));
            textSwitcher2.setText(getResources().getString(R.string.login_signup_e));
            /**
             * Captcha - pwdEditText
             */
            showView(editTextPassword);
            editTextPassword.setHint(getString(R.string.login_user_password));
            /**
             * Captcha - captchaEditText
             */
            hideView(editTextCaptcha);
            /**
             * Captcha - signUpBtn
             */
            changeBtn(signBtn);
            resetBtn(forgetBtn);
        }
        /**
         * == END IF == captcha _ register ==
         *
         * == START IF == register ==
         */
        else if (mode.equals(Mode.REGISTER)) {
            /**
             * Register - pwdEditText
             */
            showView(editTextPassword);
            editTextPassword.setHint(getString(R.string.login_user_password));
            /**
             * Register - captchaEditText
             */
            showView(editTextCaptcha);
        }

        /**
         * == END IF == register ==
         *
         * == START IF == login ==
         */
        else if (mode.equals(Mode.LOGIN)) {
            textSwitcher.setText(getResources().getString(R.string.login_backup));
            textSwitcher2.setText(getResources().getString(R.string.login_backup_e));
            /**
             * Login - pwdEditText
             */
            showView(editTextPassword);
            editTextPassword.setHint(getString(R.string.login_user_password));
            /**
             * Login - captchaEditText
             */
            hideView(editTextCaptcha);
            /**
             * Login - forgetBtn
             */
            showView(forgetBtn);
            resetBtn(forgetBtn);
            /**
             * Login - signUpBtn
             */
            showView(signBtn);
            resetBtn(signBtn);
        }

        /**
         * == END IF == login ==
         *
         * == START IF == captcha _ update ==
         */
        else if (mode.equals(Mode.UPDATE_CAPTCHA)) {
            textSwitcher.setText(getResources().getString(R.string.login_update));
            textSwitcher2.setText(getResources().getString(R.string.login_update_e));
            /**
             * Captcha - pwdEditText
             */
            hideView(editTextPassword);
            /**
             * Captcha - captchaEditText
             */
            hideView(editTextCaptcha);
            /**
             * Captcha - signUpBtn
             */
            resetBtn(signBtn);
            changeBtn(forgetBtn);
        }   /**
         *  == END IF == update _ captcha ==
         *
         * == START IF == update ==
         */
        else if (mode.equals(Mode.UPDATE)) {
            /**
             * Register - pwdEditText
             */
            showView(editTextPassword);
            /**
             * Register - captchaEditText
             */
            showView(editTextCaptcha);
        }

        /**
         * == END ELSE == else ==
         */
    }

    /**
     * Convert button mode when leave `login mode`
     *
     * @param v (Button)v the button will be changed
     */
    protected void changeBtn(View v) {
        Button btn = (Button) v;
        if (btn.getText().toString().trim().equals(getResources().getString(R.string.login_signup))
                || btn.getText().toString().trim().equals(getResources().getString(R.string.login_forget))
                || btn.getText().toString().trim().equals(getResources().getString(R.string.login_captcha))) {
            btn.setText(R.string.login_backup);
            btn.setTextColor(getResources().getColor(R.color.md_amber_400));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSubmitTitle(Mode.LOGIN);
                }
            });
        }
    }

    /**
     * Reset button mode when back to `login mode`
     *
     * @param v (Button)v the button will be changed
     */
    protected void resetBtn(View v) {
        v.setVisibility(View.VISIBLE);
        Button btn = (Button) v;
        btn.setTextColor(getResources().getColor(R.color.primary));
        if (btn.getId() == R.id.login_forget) {
            btn.setText(R.string.login_forget);
            // Don't forget reload listener
            btn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           setSubmitTitle(Mode.UPDATE_CAPTCHA);
                                       }
                                   }
            );
        } else if (btn.getId() == R.id.login_signup) {
            btn.setText(R.string.login_signup);
            // Don't forget reload listener
            btn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           setSubmitTitle(Mode.REGISTER_CAPTCHA);
                                       }
                                   }
            );
        }
    }

    /**
     * Restore size before show view
     *
     * @param v (Button|MaterialEditText)v
     */
    protected void showView(View v) {
        if (v.getId() == R.id.login_forget || v.getId() == R.id.login_signup) {
            /* showButton*/
            Log.e("showView", "It's a Button");
            // if is btn
            ViewGroup.LayoutParams lp = new AppBarLayout.LayoutParams(buttonSize.width, buttonSize.height);
            v.setLayoutParams(lp);
        } else {
            // if is edit text
            Log.e("showView", "It's a EditView");
            ViewGroup.LayoutParams lp = new AppBarLayout.LayoutParams(
                    editTextUserName.getLayoutParams().width,
                    editTextUserName.getLayoutParams().height);
            v.setLayoutParams(lp);
        }
        v.setVisibility(View.VISIBLE);
    }

    /**
     * Fix layout params before hide view v
     * @param v (Button|MaterialEditText)v
     */
    protected void hideView(View v) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.height = 0;
        v.setLayoutParams(lp);
        v.setVisibility(View.INVISIBLE);
    }

    private void login(final String phoneNumber, final String password){
        NetworkUtils.accountLogin(
                new LoginRequest(phoneNumber, password),
                new Response.Listener<MobileLoginResult>() {
                    @Override
                    public void onResponse(MobileLoginResult response) {
                        /**
                         * Only save preference when success
                         */
                        Log.e("Login", "Success!" + response.getResultCode() + response.getResultDescription());

                        Info.getInstance().user.setId(phoneNumber);
                        Info.getInstance().user.setPassword(password);
                        Info.getInstance().setToken(response.getToken());

                        loginBtn.setProgress(100);
                        LoginActivity.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                            Snackbar.make(findViewById(R.id.login_activity), r.result_description, Snackbar.LENGTH_LONG).show();
                        } catch (NullPointerException e) {
                            Snackbar.make(findViewById(R.id.login_activity), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                        } finally {
                            loginBtn.setProgress(-1);
                        }
                    }
                }
        );
    }

    private static class Mode {
        public static String REGISTER = "register";
        public static String LOGIN = "login";
        public static String UPDATE = "update";
        public static String REGISTER_CAPTCHA = "register_captcha";
        public static String UPDATE_CAPTCHA = "update_captcha";

        private Mode() {
        }
    }

}
