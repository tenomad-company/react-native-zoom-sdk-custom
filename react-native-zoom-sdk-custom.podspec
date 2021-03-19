
require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name                = package['name']
  s.version             = package['version']
  s.summary             = package['description']
  s.homepage            = package['homepage']
  s.license             = package['license']
  s.author              = package['author']
  s.platform            = :ios, "8.0"
  s.source              = { :git => "https://github.com/tenomad-company/react-native-zoom-sdk-custom.git", :tag => "master" }
  s.source_files        = "ios/**/*.{h,m,swift}"
  s.requires_arc        = true
  s.libraries           = "sqlite3", "z.1.2.5", "c++"
  s.dependency "React"
  s.xcconfig = { 'FRAMEWORK_SEARCH_PATHS' => ['"${PODS_ROOT}/ZoomSDK"', '"${PODS_ROOT}/../ZoomSDK"'] }
end
