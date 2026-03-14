SWIFTFORMAT ?= $(shell command -v swiftformat 2>/dev/null || echo /opt/homebrew/bin/swiftformat)
SWIFTLINT ?= $(shell command -v swiftlint 2>/dev/null || echo /opt/homebrew/bin/swiftlint)

SWIFT_TARGETS := HapticControllers

.PHONY: format format-check lint check

format:
	"$(SWIFTFORMAT)" $(SWIFT_TARGETS)

format-check:
	"$(SWIFTFORMAT)" --lint $(SWIFT_TARGETS)

lint:
	"$(SWIFTLINT)" lint --no-cache --config .swiftlint.yml

check: format-check lint
