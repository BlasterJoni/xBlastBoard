from jinja2 import Template
import json

templateText=""
with open("licensesTemplateForPython.html", "r") as f:
    templateText = f.read();

sources = []
with open("sources.json", "r") as f:
    sources = json.loads(f.read());

for i in sources:
    with open(i["uri"], "r") as f:
        i["license"] = f.read();

template = Template(templateText)
rendered = template.render(sources = sources)

with open("./licenses/depLicenses.html", "w+") as f:
    f.write(rendered)