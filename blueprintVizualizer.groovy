List<String> exclude = []
List<File> files = []
boolean groupByFile = true
boolean legend = true

for (int i = 0; i < args.size(); ++i) {
    if (args[i] == '-e') {
        if (i + 1 == args.size()) {
            throw new RuntimeException("-e but no exclude bean given")
        }
        exclude << args[i + 1]
        ++i
    } else if (args[i] == '--no-group-by-file') {
        groupByFile = false
    } else if (args[i] == '--no-legend') {
        legend = false
    } else {
        files << new File(args[i])
    }
}

if (!files) {
    throw new RuntimeException("No files provided")
}

println '''digraph {'''

List<String> transitions = []

files.each { f ->

    if (files.size() != 1 && groupByFile) {
        println """\tsubgraph "cluster_${f.name.take(f.name.lastIndexOf('.'))}" {"""
        println """\tlabel="${f.name}"; """
    }

    def xml = new XmlSlurper().parseText(f.text)

    def beans = xml.bean.collect {
        def b = new Bean(id: it.@id)
        b.deps.addAll(it.argument.collect {
            it.@ref.size() > 0 ? it.@ref : null
        }.findAll())
        b.deps.addAll(it.property.collect {
            it.@ref.size() > 0 ? it.@ref : null
        }.findAll())
        b.values.addAll(it.argument.collect {
            it.@value.size() > 0 ? it.@value : null
        }.findAll())
        b.values.addAll(it.property.collect {
            it.@value.size() > 0 ? it.@value : null
        }.findAll())
        if (it.'@factory-ref'.size() > 0) {
            b.factoryRef = it.'@factory-ref'.text()
        }
        if (it.'@depends-on'.size() > 0) {
            b.dependsOn = it.'@depends-on'.text().split(/\s+/)
        }
        b
    }

    def refs = xml.reference.collect {
        def b = new Bean(id: it.@id, ref: true)
        if (it.'reference-listener'.size() > 0) {
            b.listeners = it.'reference-listener'.collect { it.@ref }
        }
        b
    }

    beans.addAll(refs)

    def cmProps = xml.'cm-properties'.collect {
        new Bean(id: it.@id, cmProps: true)
    }

    beans.addAll(cmProps)

    xml.service.each { s ->
        beans.find { it.id == s.@ref.text() }?.service = true
    }

    def refLists = xml.'reference-list'.collect {
        def b = new Bean(id: it.@id, ref: true)
        if (it.'reference-listener'.size() > 0) {
            b.listeners = it.'reference-listener'.collect { it.@ref }
        }
        b
    }

    beans.addAll(refLists)


    beans.findAll { !(it.id in exclude) }.each { b ->
        if (b.ref) {
            println """\t"${b.id}"[color=yellow];"""
        } else if (b.service) {
            println """\t"${b.id}"[color=green];"""
        } else if (b.cmProps) {
            println """\t"${b.id}"[color=red];"""
        } else {
            println """\t"${b.id}";"""
        }
        b.deps.findAll { it && !(it in exclude) }.each { d ->
            transitions << """\t"${b.id}" -> "$d";"""
        }
        b.values.findAll().each { v ->
            println """\t"$v"[shape=box];"""
            transitions << """\t"${b.id}" -> "$v";"""
        }
        if (b.factoryRef && !(b.factoryRef in exclude)) {
            transitions << """\t"${b.factoryRef}" -> "${b.id}"[label="produces",color="blue"];"""
        }
        b.dependsOn.findAll { it && !(it in exclude) }.each { d ->
            transitions << """\t"${b.id}" -> "${d}"[label="depends on",color="orange"];"""
        }
        b.listeners.findAll { it && !(it in exclude) }.each { l ->
            transitions << """\t"${b.id}" -> "${l}"[label="listener",color="purple"];"""
        }
    }
    if (files.size() != 1 && groupByFile) {
        println "\t}"
    }
}

transitions.each { println it }

if(legend){
	printLegend()
}


println '''}'''

private void printLegend() {
    println '''
{ rank = sink;
    Legend [shape=none, margin=0, label=<
    <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
     <TR>
      <TD COLSPAN="2"><B>Legend</B></TD>
     </TR>
     <TR><TD>Yellow</TD><TD><FONT COLOR="yellow">reference</FONT></TD></TR>
     <TR><TD>Green</TD><TD><FONT COLOR="green">service</FONT></TD></TR>
     <TR><TD>Red</TD><TD><FONT COLOR="red">cm-properties</FONT></TD></TR>
     <TR><TD>Blue</TD><TD><FONT COLOR="blue">factory-ref</FONT></TD></TR>
     <TR><TD>Orange</TD><TD><FONT COLOR="orange">depends-on</FONT></TD></TR>
     <TR><TD>Purple</TD><TD><FONT COLOR="purple">reference-listener</FONT></TD></TR>
     <TR><TD>circle</TD><TD>bean</TD></TR>
     <TR><TD>box</TD><TD>value</TD></TR>
    </TABLE>
   >];
  }
'''
}

class Bean {
    String id
    List<String> deps = []
    boolean ref = false
    boolean service = false
    boolean cmProps = false
    List<String> dependsOn = []
    String factoryRef
    List<String> listeners = []
    List<String> values = []
}
