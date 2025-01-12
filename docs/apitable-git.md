# APITable Git版本管理指南

我们使用`git subtree`命令，与[APITable开源工程](https://github.com/apitable/apitable)进行整合。


这样可以做到，两个不同的工程，都有着各自的history，vikadata工程的更丰富一些。


## 1.将APITable工程添加到remote
```bash
# TODO: use vikadata/apitable-subtree now
git remote add -f apitable git@github.com:vikadata/apitable-subtree.git
```


```bash
# 不需要add，因为已经存在了
#git subtree add --prefix apitable apitable main --squash
```

## 2. Pull Update Remote APITable repo

```bash
git fetch apitable main
git subtree pull --prefix apitable apitable main --squash 
```

## 3. Push Update Remote APITable repo

```bash
git subtree push --prefix=apitable apitable main
```