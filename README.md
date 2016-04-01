# tvsquared-collector-android

## USAGE:

#### Initialization:

```
TVSquaredCollector.getInstance().init((Context context, String hostname, String siteid);
```
or
```
TVSquaredCollector.getInstance().init((Context context, String hostname, String siteid, boolean secure);
```

#### Events:

Simple

```
TVSquaredCollector.getInstance().track();
```

With user

```
TVSquaredCollector.getInstance().setUserId(String userId);
TVSquaredCollector.getInstance().track();
```

Complete
```
TVSquaredCollector.getInstance().setUserId("<USERID>");
track(String actionname, String product, String orderid, float revenue, String promocode)
```
