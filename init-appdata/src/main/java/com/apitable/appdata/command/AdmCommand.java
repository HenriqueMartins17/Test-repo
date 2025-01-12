package com.apitable.appdata.command;

import com.apitable.appdata.generator.service.IAppDataGenerateServer;
import com.apitable.appdata.initializer.service.IInitializerService;
import com.apitable.appdata.loader.service.IAppDataLoaderService;
import jakarta.annotation.Resource;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "adm", mixinStandardHelpOptions = true, subcommands = {
        AdmCommand.Generate.class,
        AdmCommand.Load.class,
        AdmCommand.InitUser.class,
        AdmCommand.InitConfigSpace.class
})
public class AdmCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.print("Execute adm command");
        return 0;
    }

    @Component
    @Command(name = "generate", mixinStandardHelpOptions = true)
    static class Generate implements Callable<Integer> {

        @Resource
        private IAppDataGenerateServer iAppDataGenerateServer;

        @Override
        public Integer call() {
            iAppDataGenerateServer.generate();
            return 0;
        }
    }

    @Component
    @Command(name = "load", mixinStandardHelpOptions = true)
    static class Load implements Callable<Integer> {

        @Resource
        private IAppDataLoaderService iAppDataLoaderService;

        @Override
        public Integer call() {
            iAppDataLoaderService.loadAsset();
            iAppDataLoaderService.loadData();
            return 0;
        }
    }

    @Component
    @Command(name = "init-user", mixinStandardHelpOptions = true)
    static class InitUser implements Callable<Integer> {

        @Resource
        private IInitializerService iInitializerService;

        @Override
        public Integer call() {
            iInitializerService.initUsers();
            return 0;
        }
    }

    @Component
    @Command(name = "init-config-space", mixinStandardHelpOptions = true)
    static class InitConfigSpace implements Callable<Integer> {

        @Resource
        private IInitializerService iInitializerService;

        @Override
        public Integer call() {
            iInitializerService.initConfigSpace();
            return 0;
        }
    }
}
