

Pod::Spec.new do |s|



  s.name         = "websocket"
  s.version      = "0.0.1"
  s.summary      = "eeui plugin."
  s.description  = <<-DESC
                    eeui plugin.
                   DESC

  s.homepage     = "https://eeui.app"
  s.license      = "MIT"
  s.author             = { "veryitman" => "aipaw@live.cn" }
  s.source =  { :path => '.' }
  s.source_files  = "websocket", "**/**/*.{h,m,mm,c}"
  s.exclude_files = "Source/Exclude"
  s.resources = 'websocket/resources/image/**'
  s.platform     = :ios, "8.0"
  s.requires_arc = true

  s.dependency 'WeexPluginLoader', '~> 0.0.1.9.1'

end
