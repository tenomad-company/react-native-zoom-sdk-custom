require "json"
package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNZoomUs"
  s.version      = package["version"]
  s.summary      = "RNZoomUs"
  s.description  = <<-DESC
                  RNZoomUs
                   DESC
  s.homepage     = "https://zoom.us/"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://github.com/tientruongvan1995/react-native-zoom-us.git", :tag => "master" }
  s.source_files  = "ios/**/*.{h,m}"
  s.requires_arc = true
  s.libraries = "sqlite3", "z.1.2.5", "c++"
  s.vendored_frameworks = "ios/libs/MobileRTC.framework", "ios/libs/MobileRTCScreenShare.framework"

  s.dependency "React"
  #s.dependency "others"

end

  