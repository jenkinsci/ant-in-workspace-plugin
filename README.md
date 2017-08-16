# Jenkins Plugin - Ant In Workspace

## Official Documentation
The user documentation is available here
* https://plugins.jenkins.io/ant-in-workspace

In this README you will find more technical information for Developers that are interested in supporting or enhancing this plugin.

## What does this  this?
This Plugin extends the official Jenkins Ant Plugin. It provides a new Builder based on Ant that has disabled the functionality to choose between different Ant versions. But it will allow to use an Ant version that is available in the workspace.

This is to allow (legacy) builds to use a special Ant that is checked into the SCM. When building the Job this special Ant version is returned and file-permissions (+x) are set. It is possible to configure the path to Ant in a global AntInWorkspace parameter or a per-Job parameter.

## Documentation
The user documentation is available here
* https://plugins.jenkins.io/ant-in-workspace

# Continuous Delivery
https://ci.jenkins.io/job/Plugins/job/ant-in-workspace-plugin/

# Technical Shortcuts
```
curl -kv --no-proxy 192.168.56.101:8080 http://192.168.56.101:8080/pluginManager/uploadPlugin -u admin:admin -F file=@target/ant-in-workspace.hpi
curl -kv --no-proxy 192.168.56.101:8080 http://192.168.56.101:8080/safeRestart -u admin:admin -F Submit=Yes -X POST
```
## Authors
Stephan Watermeyer

## License
Licensed under the [MIT License (MIT)](https://github.com/heremaps/buildrotator-plugin/blob/master/LICENSE).
