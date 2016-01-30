QWAZR Library
================

The library is a set of tools and connectors available in a QWAZR application.

### Location of the configuration files

These objects are defined in JSON files located in the **etc** directory on the ROOT of a QWAZR application.

It is possible to group set of configuration files in a subdirectory. Therefore the environnement variable "QWAZR_ETC" defines which subdirectories will be loaded. 

A typical **etc** structure:

```
my-app
|-- etc
    | -- global.json
    | -- dev
         | -- tools.json
         | -- connectors.json
    | -- production
         | -- tools.json
         | -- connectors.json
    | -- disabled
         | -- testing.json
```

- Configuration files in the root of **etc** are always loaded: **global.json**
- Configuration files in the a sub directory are loaded if theirs names are set in a QWAZR_ETC environment variable.: ```QWAZR_ETC="dev"```

### The content of a configuration file

Each library object is defined by its Java class and a set of customized properties.

Here is an example of **etc/global.json** configuration file:


```json
{
  "library": [
    {
      "name": "freemarker",
      "class": "com.qwazr.tools.FreeMarkerTool"
    },
    {
      "name": "markdown",
      "class": "com.qwazr.tools.MarkdownTool",
      "extensions": [
        "hardwraps",
        "autolinks",
        "fenced_code_blocks",
        "atxheaderspace"
      ]
    },
    {
      "name": "properties",
      "class": "com.qwazr.tools.PropertiesTool",
      "path": "site.properties"
    }
  ]
}
```

### Usage with Javascript

In your Javascript application, these objects are exposed by the global variable **qwazr**.

```js
//Use the Markdown tool to convert my Markdown file as HTML
var html = qwazr.markdown.toHtml('my_files/README.md')

// Put the HTML as an attribute of the request object
request.attributes.currentfile = html

// Use the Freemarker tool to display the template
qwazr.freemarker.template("templates/my_template.ftl", request, response)
```

### Usage with Java

In a JAVA application, these objects are injected using annotations:

```java

import com.qwazr.library.Qwazr;

public class MyClass {

    @qwazr("freemarker")
    FreemarkerTool freemarker;
}

```


