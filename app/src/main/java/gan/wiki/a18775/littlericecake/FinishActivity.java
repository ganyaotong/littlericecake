package gan.wiki.a18775.littlericecake;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        this.finish();
    }
}
