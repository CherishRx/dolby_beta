package com.raincat.dolby_beta;

import android.content.Context;

import com.raincat.dolby_beta.hook.AdAndUpdateHook;
import com.raincat.dolby_beta.hook.AutoSignInHook;
import com.raincat.dolby_beta.hook.BlackHook;
import com.raincat.dolby_beta.hook.DownloadMD5Hook;
import com.raincat.dolby_beta.hook.EAPIHook;
import com.raincat.dolby_beta.hook.GrayHook;
import com.raincat.dolby_beta.hook.HideTabHook;
import com.raincat.dolby_beta.hook.InternalDialogHook;
import com.raincat.dolby_beta.hook.MagiskFixHook;
import com.raincat.dolby_beta.hook.OverseaHook;
import com.raincat.dolby_beta.hook.ProfileHook;
import com.raincat.dolby_beta.hook.CookieHook;
import com.raincat.dolby_beta.hook.SidebarCutHook;
import com.raincat.dolby_beta.hook.TaiChiFixHook;
import com.raincat.dolby_beta.hook.TestHook;
import com.raincat.dolby_beta.utils.CloudMusicPackage;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;

import net.androidwing.hotxposed.IHookerDispatcher;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : Hook入口
 *     version: 1.0
 * </pre>
 */

public class HookerDispatcher implements IHookerDispatcher {
    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(findClass("com.netease.cloudmusic.NeteaseMusicApplication", lpparam.classLoader),
                "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!Setting.isEnable()) {
                            return;
                        }

                        final Context neteaseContext = (Context) param.thisObject;
                        final String processName = Tools.getCurrentProcessName(neteaseContext);

                        if (processName.equals(CloudMusicPackage.PACKAGE_NAME)) {
                            CloudMusicPackage.init(neteaseContext);
                            //黑胶
                            if (Setting.isBlackEnabled()) {
                                Tools.deleteDirectory(CloudMusicPackage.CACHE_PATH);
                                Tools.deleteDirectory(CloudMusicPackage.CACHE_PATH2);
                                new BlackHook(neteaseContext, CloudMusicPackage.versionCode);
                                new DownloadMD5Hook();
                                new EAPIHook(neteaseContext);
                            }
                            //自动签到
                            if (Setting.isAutoSignInEnabled())
                                new AutoSignInHook(neteaseContext, CloudMusicPackage.versionCode);
                            //不变灰
                            if (Setting.isGrayEnabled())
                                new GrayHook(neteaseContext);
                            //干掉内测弹窗
                            if (Setting.isInternalEnabled())
                                new InternalDialogHook(neteaseContext, CloudMusicPackage.versionCode);
                            //海外模式
                            if (Setting.isOverseaModeEnabled())
                                new OverseaHook(neteaseContext, CloudMusicPackage.versionCode);
                            //Magisk冲突
                            if (Setting.isMagiskEnabled())
                                new MagiskFixHook(neteaseContext);
                            //去广告与升级
                            new AdAndUpdateHook(neteaseContext, CloudMusicPackage.versionCode);
                            //修复太极优化后无法hook的bug
                            new TaiChiFixHook();
                            new CookieHook(neteaseContext);
                            //精简Tab
                            if (Setting.isHideTabEnabled())
                                new HideTabHook(neteaseContext, CloudMusicPackage.versionCode);
                            //精简侧边栏
                            new SidebarCutHook(neteaseContext, CloudMusicPackage.versionCode);
                            //伪装个人信息
                            new ProfileHook(neteaseContext);
                            new TestHook(neteaseContext);
                        } else if (processName.equals(CloudMusicPackage.PACKAGE_NAME + ":play")) {
                            CloudMusicPackage.init(neteaseContext);
                            if (Setting.isBlackEnabled()) {
                                new EAPIHook(neteaseContext);
                            }
                            if (Setting.isOverseaModeEnabled())
                                new OverseaHook(neteaseContext, CloudMusicPackage.versionCode);
                            new AdAndUpdateHook(neteaseContext, CloudMusicPackage.versionCode);
                            new TaiChiFixHook();
                        }
                    }
                });
    }
}
