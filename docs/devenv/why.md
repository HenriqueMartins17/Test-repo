# 为什么要用云原生的方式搭建开发环境？


2022年9月开始，vikadata工程改造成Cloud Native云原生的开发环境，本地开发和IDE都建议基于容器运行。


这是出于以下几点考虑：

1. **易用性**。之后我们会进行代码开源，与自己同事不同，社区用户对代码工程的易用性极其挑剔，他们没有那么有耐性，复杂难入门的工程，会影响外界的代码贡献，我们需要帮助他们在5分钟之内，从无到有地把代码运行起来；
2. **统一性**。过去我们在macOS、Linux中，由各人自己搭建开发环境，混杂不同的操作系统、编程语言版本、ARM/x86架构不同等等，开发环境与生产环境的“环境”是不同的，在一个环境产生的BUG，不代表另一边正常，这加重了测试负担；
3. **适应性**。在过去，我们同一份代码，在面向不同的人群，是完全不同的架构。比如，工程师有自己搭建的环境、私有化客户有另起炉灶的PoC编译、SaaS版本有独立的Terraform版本、开发版、私有化版、SaaS版各不相同，研发、运维、客户、用户，分别有着类似的、低水平重复的工作量，隐含有巨大的浪费；


因此，我们希望帮助以下人群，做到：
1. **社区开发者**，在拿到开源版工程的3分钟之内，把vika维格表完整地跑起来；
2. **研发工程师**，能在3分钟之内，在本地电脑（或云主机）用docker-compose搭建完整的跟SaaS版vika维格表功能完全一致开发环境；
3. **全体vika维格员工**，或新人，能在3分钟之内，运行自己的vika维格表；
4. **私有化客户**的运行环境，直接使用研发工程的docker-compose版本，本地开发环境与生产环境是一致的、无额外工作量的，能在10分钟之内帮助1000个的私有化客户更新到最新版本和环境；
5. **运维开发工程师**，工作量大大降低，因为docker-compose版本，是由全体工程门集体维护的，而不是独立重复的工作量；
6. **研发工程师**，更方便地做远程调试、跨环境调试，比如调试别人机器上的代码等；


有趣的是，**把复杂的开发工程做得简单**，其实是个最不简单的事情，是技术界的常见难题之一，它极度影响研发团队的沟通成本，有时候不是技术能力不行，而是架构搭得太烂了，导致新人被拖累，请大家对这件事上心。

> P.S. 2022年9月之前，没有任何人能自己启动功能完整、模板完备的vika维格表。

## Intellij IDEA


Intellij IDEA打开vikadata目录。

如何Debug断点调试？

官方文档参考：[Intellij IDEA Spring Boot and docker-compose containers](https://www.jetbrains.com/help/idea/run-and-debug-a-spring-boot-application-using-docker-compose.html#clone_sample_project)

## Visual Studio Code

Visual Studio Code打开vikadata目录。

如何Debug断点调试？

官方文档参考：[Visual Studio and docker-compose containers](https://code.visualstudio.com/docs/containers/docker-compose)