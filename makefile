# ------------------------------------------------------------------------------

default: build

# SPEC -------------------------------------------------------------------------

NAME    := triemap
VERSION := 0.1.0

# PATHS ------------------------------------------------------------------------

ifeq ($(OS) , Windows_NT)
SEP := ;
else
SEP := :
endif

output  := out/production/$(NAME)
test    := out/test/$(NAME)
cp      := "$(output)"
testcp  := "$(output)$(SEP)$(test)"

# CLEAN ------------------------------------------------------------------------

clean:
	rm -rf out

# BUILD & TEST -----------------------------------------------------------------

build:
	mkdir -p $(output)
	kotlinc -cp $(cp) src -d $(output)
	mkdir -p $(test)
	kotlinc -cp $(testcp) test -d $(test)

test:
	kotlin -cp $(testcp) norswap.triemap.TestKt

# JAR --------------------------------------------------------------------------

jar:
	find out -name .DS_Store -type f -delete
	jar cf out/$(NAME)-$(VERSION).jar -C $(output) .

# ------------------------------------------------------------------------------

.PHONY: \
  default \
  clean \
  build \
  build-tests \
  test \
  jar

# ------------------------------------------------------------------------------
