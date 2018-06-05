package com.cloudminds.register.repository.network;

import android.widget.Toast;

import com.cloudminds.register.BasicApp;
import com.cloudminds.register.R;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.functions.Consumer;

/**
 * Created
 */

public class ErrorConsumer<T> implements Consumer<T> {

    String result;

    @Override
    public void accept(T t) throws Exception {
        if (t instanceof UnknownHostException) {
            result = BasicApp.getContext().getString(R.string.request_network_error);
        } else if (t instanceof SocketTimeoutException) {
            result = BasicApp.getContext().getString(R.string.request_network_error);
        } else if (t instanceof ConnectException) {
            result = BasicApp.getContext().getString(R.string.request_network_error);
        } else {
            result = BasicApp.getContext().getString(R.string.request_error);
        }
        Toast.makeText(BasicApp.getContext(), result, Toast.LENGTH_SHORT).show();
    }
}
