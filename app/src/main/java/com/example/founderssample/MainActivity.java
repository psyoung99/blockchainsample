package com.example.founderssample;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.samsung.android.sdk.blockchain.CoinType;
import com.samsung.android.sdk.blockchain.ListenableFutureTask;
import com.samsung.android.sdk.blockchain.SBlockchain;
import com.samsung.android.sdk.blockchain.account.Account;
import com.samsung.android.sdk.blockchain.account.ethereum.EthereumAccount;
import com.samsung.android.sdk.blockchain.coinservice.CoinNetworkInfo;
import com.samsung.android.sdk.blockchain.coinservice.CoinServiceFactory;
import com.samsung.android.sdk.blockchain.coinservice.ethereum.EthereumService;
import com.samsung.android.sdk.blockchain.exception.SsdkUnsupportedException;
import com.samsung.android.sdk.blockchain.network.EthereumNetworkType;
import com.samsung.android.sdk.blockchain.ui.CucumberWebView;
import com.samsung.android.sdk.blockchain.ui.OnSendTransactionListener;
import com.samsung.android.sdk.blockchain.wallet.HardwareWallet;
import com.samsung.android.sdk.blockchain.wallet.HardwareWalletType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnSendTransactionListener {

    Button connnectBtn;
    Button generateAccountBtn;
    Button getAccountBtn;
    Button paymentsheetBtn;
    Button sendSmartContractBtn;
    Button webViewInitBtn;
    private SBlockchain sBlockchain;
    private HardwareWallet wallet;
    private Account generateAccount;
    private CucumberWebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //sBlockchain 객체 선언
        sBlockchain = new SBlockchain();
        try {
            sBlockchain.initialize(this);
        } catch(SsdkUnsupportedException e) {
            e.printStackTrace();
        }

        connnectBtn = findViewById(R.id.connect);
        generateAccountBtn = findViewById(R.id.generateAccount);
        getAccountBtn = findViewById(R.id.getAccount);
        paymentsheetBtn = findViewById(R.id.paymentsheet);
        sendSmartContractBtn = findViewById(R.id.sendSmartContract);
        webViewInitBtn = findViewById(R.id.webViewInit);
        webView = findViewById(R.id.cucumberWebview);


        //connect 버튼 클릭
        connnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        //generateAccount 버튼 클릭
        generateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generate();
            }
        });

        //getAccount 버튼 클릭
        getAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAccount();
            }
        });

        //paymentsheet 버튼 클릭
        paymentsheetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentSheet();
            }
        });

        //webViewInit 버튼 클릭
        webViewInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webViewInit();
            }
        });
    }


    //Hot wallet-internet 연결, cold wallet-internet 연결X, keystore에 private key 부탁
    //private key 가져오기 위해 Hardwarewallet connect하기(account는 private key로 만든 address필요, cold wallet에 key 있음)
    private void connect() {
        sBlockchain.getHardwareWalletManager()
                .connect(HardwareWalletType.SAMSUNG,true).setCallback(new ListenableFutureTask.Callback<HardwareWallet>() {
                    @Override
                    public void onSuccess(HardwareWallet hardwareWallet) {
                        wallet = hardwareWallet;
                    }

                    @Override
                    public void onFailure(ExecutionException e) {

                    }

                    @Override
                    public void onCancelled(InterruptedException e) {

                    }
                });
    }

    //account 생성. 하드웨어 갔다 오므로 비동기식 callback 위해 setCallback 생성
    public void generate() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH, EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );
        sBlockchain.getAccountManager()
                .generateNewAccount(wallet, coinNetworkInfo)
                .setCallback(new ListenableFutureTask.Callback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        generateAccount = account;
                        Log.d("Myapp",account.toString());
                    }

                    @Override
                    public void onFailure(@NotNull ExecutionException e) {
                        Log.d("Myapp",e.toString());
                    }

                    @Override
                    public void onCancelled(@NotNull InterruptedException e) {

                    }
                });
    }

    //account 가져오기. unique id 만들어짐
    public void getAccount() {
        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(), CoinType.ETH, EthereumNetworkType.ROPSTEN);
        Log.d("Myapp", Arrays.toString(new List[]{accounts}));
    }

    public void paymentSheet() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH, EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );

        //account 전달
        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(),CoinType.ETH,EthereumNetworkType.ROPSTEN);
        EthereumService ethereumService = (EthereumService) CoinServiceFactory.getCoinService(this,
                coinNetworkInfo);
        Intent intent = ethereumService.createEthereumPaymentSheetActivityIntent(
                this,wallet,(EthereumAccount)accounts.get(0),
                "0x8185513c848c0D9757169a90AcCEaB0b84E123C7",
                new BigInteger("1000000000000000"),
                null,
                null
        );

        startActivityForResult(intent,0);
    }

    public void webViewInit() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH, EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );

        //account 전달
        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(),CoinType.ETH,EthereumNetworkType.ROPSTEN);
        EthereumService ethereumService = (EthereumService) CoinServiceFactory.getCoinService(this,
                coinNetworkInfo);

        webView.init(ethereumService, accounts.get(0),this);
        webView.loadUrl("https://faucet.metamask.io");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onSendTransaction(@NotNull String requestId,
                                  @NotNull EthereumAccount fromAccount,
                                  @NotNull String toAddress,
                                  @org.jetbrains.annotations.Nullable BigInteger value,
                                  @org.jetbrains.annotations.Nullable String data,
                                  @org.jetbrains.annotations.Nullable BigInteger nonce
    ) {
        HardwareWallet connectedHardwareWallet = sBlockchain.getHardwareWalletManager()
                .getConnectedHardwareWallet();

        Intent intent = webView.createEthereumPaymentSheetActivityIntent(
                this,requestId,connectedHardwareWallet,toAddress,
                value,
                data,
                nonce
        );

        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != 0) {
            return;
        }

        webView.onActivityResult(requestCode,resultCode,data);
    }
}
