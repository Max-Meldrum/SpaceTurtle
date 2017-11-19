[![Coverage Status](https://coveralls.io/repos/github/Max-Meldrum/SpaceTurtle/badge.svg?branch=master)](https://coveralls.io/github/Max-Meldrum/SpaceTurtle?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/Max-Meldrum/SpaceTurtle.svg?branch=master)](https://travis-ci.org/Max-Meldrum/SpaceTurtle)

# SpaceTurtle

Development ongoing.


Set up ZooKeeper for development
```
$ ./docker/zookeeper_setup.sh
```

Compile SpaceTurtle
```
$ ./compile.sh
```

Run Master
```
$ ./bin/master.sh
```

Run Agent
```
$ ./bin/agent.sh
```

Run the tests with enabled coverage
```
$ sbt clean coverage test
```

Generate coverage reports
```
$ sbt coverageReport
$ sbt coverageAggregate # To get summary of all projects
```


# Tasks

- [x] Join cluster through ZooKeeper
- [x] Set up auth to ZooKeeper

# License
SpaceTurtle is Open Source and available under the Apache 2 License.







