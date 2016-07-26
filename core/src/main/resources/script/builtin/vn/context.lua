
---Functions related to the current script context
-- 
module("vn.context", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- Init dummy prefs if needed
prefs = prefs or {}

-- ----------------------------------------------------------------------------
--  Local Functions
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

function getScreen()
	return getCurrentContext():getScreen()
end

function getTextState()
	return getScreen():getTextState()
end

function getRenderEnv()
    return getScreen():getRenderEnv()
end

function getEffectSpeed()
    local speed = prefs.effectSpeed or 1
    if isSkipping() then
        speed = speed * 4
    end
    return speed
end

-- ----------------------------------------------------------------------------
--  Skip functions
-- ----------------------------------------------------------------------------

function getSkipState()
	return getCurrentContext():getSkipState()
end

function isSkipping()
    return getSkipState():isSkipping()
end

function shouldSkipLine()
    return getSkipState():shouldSkipLine(isLineRead())
end

function getSkipMode()
    return getSkipState():getSkipMode()
end

function setSkipMode(mode)
    return getSkipState():setSkipMode(mode)
end

function stopSkipping()
    return getSkipState():stopSkipping()
end

---Waits for the specified time to pass. Time progression is influenced by the current
-- <code>effectSpeed</code> and the wait may be cancelled by holding the skip key or pressing the text
-- continue key.
-- @number durationFrames The wait time in frames (default is 60 frames per second).
function wait(durationFrames)
    while durationFrames > 0 do
        if shouldSkipLine() or Input.consume(VKeys.textContinue) then
            break
        end
        durationFrames = durationFrames - getEffectSpeed()
        yield()
    end    
end

---Waits until the text continue key is pressed. Skipping ignores the wait.
function waitClick()
    while not shouldSkipLine() and not Input.consume(VKeys.textContinue) do
        yield()
    end
end
