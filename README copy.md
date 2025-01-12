
# Get started

`vikadata` is a cloud-native-based development project.

All commands runs under `containers`.

Setup the environment and run/debug it with Makefile commands.

## Quick Start

vikadata" project is based on "APITable" and requires basic environment configuration. Please make sure to read the [APITable Developer Guide](https://github.com/apitable/apitable/blob/develop/docs/contribute/developer-guide.md)

At this point, you are ready to begin：
- Docker
- Makefile
- NodeJS
- Java
- Rust


Get started quickly and try running now!

```bash
make e # First, enter the 'make edition' command and select option 3 for 'vika-ee'. Then, enter the 'make env' command and select option 1 for 'local-db'.
make dataenv # start docker database
make install # Start installing dependencies
make run # Choose 1, 2, 3, 4 for multiple windows respectively
make build # natively compiled program
make test # Full unit testing

# For more detailed commands, see the Makefile
```

`make e` This command is equivalent to 'make env + make edition + make edition-settings'

`make edition`make edition provides multiple editions to switch between, including:
> 1. apitable-ce (default): APITable open-source edition
> 2. apitable-ee: APITable enterprise SaaS edition

`make env` is used to set up the development environment, and there are five different modes available, with the "local" mode used above.

`make edition-settings` is used to configure settings and logos for different editions.
`make install` is used to install dependencies.

## Are you developer?

We provide five modes for setting up the development environment：

1. **docker-all**: Runs all local code services using Docker and starts a local database environment (dataenv). This mode is compatible with the 'make up' command and is the only mode available for the open-source edition.
2. **local-db**: Starts only the Docker database environment without starting any application services, making it convenient for you to develop using VSCode or IntelliJ IDEA.
3. **remote-db**: Redirects development series database traffic to the local environment, making it convenient for you to develop using VSCode or IntelliJ IDEA.
4. **integration**: Redirects integration services and traffic to the local environment, and local debug programs will connect to the integration environment by default.
5. **testing**: Redirects testing services and traffic to the local environment, and local debug programs will connect to the testing environment by default.

Please refer to the: [⭐️ Developer's Guide to Using the 5 Development Environments](./docs/devenv/devenv.md)，step-by-step instructions on setting up the five different development environments.

## Are you QA or PD?

If you are a product manager or a tester and only want to run the entire APITable locally on your computer (production environment, no compilation, directly using production containers):

```bash
# Download all production containers from the internet; you need to prepare the download key in advance
CR_PAT=$CR_PAT_GITHUB_TOKEN make pull

# Start up according to whether your machine is arm64 or amd64
make up

# If you are using a macOS M1 ARM chip and want to force x86 architecture to run
make up-amd64

# Conversely, x86 Docker also supports running on ARM
make up-arm64
```

That's all there is to it! Now, you can access APITable by visiting [https://localhost:80](https://localhost:80) .

Oh, and you probably often need to create templates (init-template) and configuration files (init-settings) in your daily work.

```bash
make settings
make templates
```

## Full-stack Local Development Environment

If you are a developer and want to set up a local development environment, and you are a big fan of TDD (test-driven development), you may not even need to debug or use an IDE. In this case, you can purely use cloud-native development mode, using containers as your runtime to debug and run local code on your computer:

```bash
# install dependencies
make install

# start up data services and dev environment
make dataenv
make devenv

# follow devenv all console logs
make devenv-logs

# view devenv services
make devenv-ps
# view all services
make ps

# shutdown all
make devenv-down
make dataenv-down
```

At this point, the data services (MySQL+MongoDB+Redis+...) will start up and the local code will run.
Yes, it consumes a lot of memory, so it is often unnecessary to run all services. If you only want to run a particular service:

```bash
# Only run one service...
make install-socket-server
make devenv-socket-server
```

> It runs under cloud-native containers and also supports breakpoint debugging in Intellij IDEA and Visual Studio Code. For more details, please refer to: [Intellij IDEA & Visual Studio Code](./docs/devenv/why.md).

## If you want to simulate CI operation?


If you want to simulate automated CI operation and compile all containers on your local computer and push them to the image server in one go:

```
CR_PAT=$CR_PAT_GITHUB_TOKEN make pull
make buildpush
```



For more function，enter:
```bash
make 
```

For more information：[Why Build Development Environment in Cloud-Native Way?](./docs/devenv/why.md)

## If you are responsible for version tagging and release

Please ensure that the version is incremented and generate a changelog:
```bash
make bumpversion
# Select which version number to modify, and it will be modified in batches

make changelog
# Generate CHANGELOG.md, manually modify the final result, and tag after completion
```

## Connect to cloud database

```
make ports
make ports-pro # use port forward to connect our databases on cloud, require k8s cert
```

## Code or Configuration Generator (make gen)

You can use the following command to generate code, configuration files, design materials, etc.:
```bash
make gen
```

These configurations are usually generated by reading the configuration in the APITable using the API, and there will be 5 options to choose from:

1) settings-ce:
2) settings-SaaS
3) proto:
4) design:
5) edition-settings:
   a. apitable
   b. vika
   c. ....
6) templates-pack:

## Peripheral Tools

During the development process, we will constantly extract some components as independent development tools (or open-source), and the following is a summary:
- [apphook](https://github.com/apitable/apphook): EventManager event engine
- [github-changelog-builder](https://github.com/apitable/github-changelog-builder): CHANGELOG generator
- [apitable-settings-generator](https://github.com/apitable/apitable-settings-generator): Configuration generator
- [apitable-i18-generator](https://github.com/apitable/apitable-i18n-generator): Multi-language configuration generator and loading library

# Documentation

Vikadata is a Commercial version of Open Source [APITable](https://github.com/apitable/apitable) which added full enterprise-ready features:

- Social login, such as WeChat/Feishu/WeCom/DingTalk/Slack/GMail, etc.
- Document attachments and other plugins
- SAML/OAuth account authentication
- Audit
- SSO Single Sign-On
- Advanced Embedding
- apitable.com SaaS environment development integration


Also read our public Developers Center website:
- [Developer Center](https://developers.apitable.com)

And you can read [`Contributing`](#contributing) chapter for more general development docs.

## Structure

Vikadata project file structures:
```bash
.
├── apitable # Open Source Project APITable, which will be split to a independent repo later.
│  ├── init-db # init container that init databases schema 
│  ├── backend-server # Java Spring Boot based `backend-server` container
│  └── packages # nodejs packages
│      ├── datasheet # `web-server` container
│      ├── room-server # `room-server` container
│      └── socket-server # `socket-server` container
├── CHANGELOG.md # version update information, releases changelogs
├── SECURITY.md # Security Policy and reporting vulnerability
├── LICENSE.md # Open source license, commercial license, embedding license, enterprise license, Cloud(SaaS) 
├── docs # Documentation and static resources files.
```

More contributing details:

- [List and Differences of Make Commands](./docs/makefile/makefile.md)
- [View Product Backlogs](#)
- [Frontend & NodeJS Development Notes](./docs/frontend.md)
- [Business Configuration Table & Multi-language Translation](./docs/settings.md)
- [Sentry & Bug Monitoring](./docs/sentry.md)
- [APITable Git Version Management Guide](./docs/apitable-git.md)
- [How to Create an AutomationAction](./docs/action/en-US/how-to-create-automation-action.md)
- [Environment Configuration](./docs/env_variable.md)
- [Database Schema UML](./docs/database.md)

# Get involved

- [APITable Github Home](https://github.com/apitable)
- [APITable.com](https://apitable.com)

## Debug Tool

- Sentry: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-sentry
- LangSmith: https://smith.langchain.com/
- K8s: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-k8s
- ELK: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-kibana
- 1Password: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-1pasword
- Vika-auth: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-vika-auth
- Github: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-github-1
- APIDocs: https://apitable.getoutline.com/doc/2023click-me-wiuLHMyILr#h-api-docs

# License

Vikadata is under Commercial License. See: [LICENSE.md](./LICENSE.md)

Copyright &copy; 2022 **Vika, inc. (Hong Kong)** and **APITable PTE.LTD. (Singapore)**
