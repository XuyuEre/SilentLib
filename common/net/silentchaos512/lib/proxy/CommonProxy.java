package net.silentchaos512.lib.proxy;

import net.silentchaos512.lib.registry.SRegistry;

public class CommonProxy {

  public CommonProxy() {

  }

  public void preInit(SRegistry registry) {

    registry.preInit();
  }

  public void init(SRegistry registry) {

    registry.init();
  }

  public void postInit(SRegistry registry) {

    registry.postInit();
  }
}
