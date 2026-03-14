SWIFTFORMAT ?= $(shell command -v swiftformat 2>/dev/null || echo /opt/homebrew/bin/swiftformat)
SWIFTLINT ?= $(shell command -v swiftlint 2>/dev/null || echo /opt/homebrew/bin/swiftlint)
XCODEBUILD ?= xcodebuild
DEVICECTL ?= xcrun devicectl
RUBY ?= /usr/bin/ruby

SWIFT_TARGETS := HapticControllers
PROJECT ?= HapticControllers.xcodeproj
SCHEME ?= HapticControllers
CONFIGURATION ?= Debug
DERIVED_DATA ?= .deriveddata
APP_NAME ?= HapticControllers.app
APP_PATH ?= $(DERIVED_DATA)/Build/Products/$(CONFIGURATION)-iphoneos/$(APP_NAME)
BUNDLE_ID ?= com.ZoonZoon
DEVICE ?=
DEVICE_JSON ?= /tmp/hapticcontrollers-devices.json

.PHONY: format format-check lint check run run-console

format:
	"$(SWIFTFORMAT)" $(SWIFT_TARGETS)

format-check:
	"$(SWIFTFORMAT)" --lint $(SWIFT_TARGETS)

lint:
	"$(SWIFTLINT)" lint --no-cache --config .swiftlint.yml

check: format-check lint

run: LAUNCH_MODE :=
run-console: LAUNCH_MODE := --console

run run-console:
	@set -eu; \
	$(DEVICECTL) list devices --json-output "$(DEVICE_JSON)" >/dev/null; \
	DEVICE_ID="$$( \
		"$(RUBY)" -rjson -e ' \
json = JSON.parse(File.read(ARGV[0])); \
needle = ARGV[1].to_s; \
devices = json.fetch("result").fetch("devices").select do |device| \
  device.dig("hardwareProperties", "deviceType") == "iPhone" && \
    device.dig("connectionProperties", "tunnelState") == "connected" && \
    device.dig("deviceProperties", "developerModeStatus") == "enabled"; \
end; \
if !needle.empty?; \
  devices.select! do |device| \
    [ \
      device["identifier"], \
      device.dig("hardwareProperties", "udid"), \
      device.dig("deviceProperties", "name") \
    ].compact.include?(needle); \
  end; \
end; \
if devices.empty?; \
  detail = needle.empty? ? "No connected iPhone with Developer Mode enabled found." : "No connected iPhone matched DEVICE=#{needle}."; \
  abort(detail); \
end; \
if devices.size > 1; \
  abort("Multiple connected iPhones found. Use make run DEVICE=<name|udid|identifier>."); \
end; \
print devices.first.dig("hardwareProperties", "udid") \
' "$(DEVICE_JSON)" "$(DEVICE)" \
	)"; \
	echo "Using device $$DEVICE_ID"; \
	"$(XCODEBUILD)" -project "$(PROJECT)" -scheme "$(SCHEME)" -configuration "$(CONFIGURATION)" -destination "id=$$DEVICE_ID" -derivedDataPath "$(DERIVED_DATA)" build; \
	$(DEVICECTL) device install app --device "$$DEVICE_ID" "$(APP_PATH)"; \
	$(DEVICECTL) device process launch --device "$$DEVICE_ID" --terminate-existing $(LAUNCH_MODE) "$(BUNDLE_ID)"
