---
id: vn/video.lua
title: vn/video.lua
---

<!--excerpt-->

{% include sourcecode.html id="textnotation" lang="lua" class="full-screen" content="---Video support.
-- 
module(\"vn.video\", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Plays a full-screen video, pauses the main thread while it plays.
-- @string filename Path to a valid video file (relative to <code>res/video</code>). Supported video formats
--         are platform dependent.
function movie(filename)
    local video = Video.movie(filename)
    repeat
        yield()
    until video == nil or video:isStopped() or Input.consume(VKeys.cancel)
    
    -- Stop video if cancelled
    if video ~= nil and not video:isStopped() then
        video:stop()
    end
end
" %}
                