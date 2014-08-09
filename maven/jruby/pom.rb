require 'rexml/document'
require 'rexml/xpath'

doc = REXML::Document.new File.new(File.join(File.dirname(__FILE__),'..', '..', 'pom.xml'))
version = REXML::XPath.first(doc, "//project/version").text

project 'JRuby Main Maven Artifact' do

  model_version '4.0.0'
  id "org.jruby:jruby:#{version}"
  inherit "org.jruby:jruby-artifacts:#{version}"
  packaging 'jar'

  properties( 'tesla.dump.pom' => 'pom-generated.xml',
              'jruby.basedir' => '${basedir}/../../',
              'main.basedir' => '${project.parent.parent.basedir}' )

  jar 'org.jruby:jruby-core:${project.version}'
  jar 'org.jruby:jruby-stdlib:${project.version}'

  plugin( :source,
          'skipSource' =>  'true' )
  plugin 'org.codehaus.mojo:build-helper-maven-plugin' do
    execute_goals( 'attach-artifact',
                   :id => 'attach-artifacts',
                   :phase => 'package',
                   'artifacts' => [ { 'file' =>  '${basedir}/src/empty.jar',
                                      'classifier' =>  'sources' },
                                    { 'file' =>  '${basedir}/src/empty.jar',
                                      'classifier' =>  'javadoc' } ] )
  end

  plugin 'org.apache.felix:maven-bundle-plugin'
  plugin :jar do
    execute_goals( 'jar',
                   :id => 'default-jar',
                   :phase => 'package',
                   'archive' => {
                     'manifestFile' =>  '${project.build.outputDirectory}/META-INF/MANIFEST.MF'
                   } )
  end

  plugin( :invoker,
          'projectsDirectory' =>  'src/it',
          'cloneProjectsTo' =>  '${project.build.directory}/it',
          'preBuildHookScript' =>  'setup.bsh',
          'postBuildHookScript' =>  'verify.bsh' ) do
    execute_goals( 'install', 'run',
                   :id => 'integration-test',
                   'settingsFile' =>  '${basedir}/src/it/settings.xml',
                   'localRepositoryPath' =>  '${project.build.directory}/local-repo' )
  end

end
