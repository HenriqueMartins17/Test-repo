package com.apitable.appdata.command;


import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class AdmCommandRunner implements CommandLineRunner, ExitCodeGenerator {

    private final AdmCommand command;

    // auto-configured to inject PicocliSpringFactory
    private final IFactory factory;

    private int exitCode;

    @Resource
    private ApplicationContext applicationContext;

    public AdmCommandRunner(AdmCommand command, IFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(command, factory).execute(args);
        System.exit(SpringApplication.exit(applicationContext));
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}