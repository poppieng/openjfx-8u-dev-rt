/*
 * Copyright (C) 2014 Igalia S.L.
 * Copyright (C) 2014 Apple Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. AND ITS CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE INC. OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef Counters_h
#define Counters_h

struct CopyMoveCounter {
    static unsigned constructionCount;
    static unsigned copyCount;
    static unsigned moveCount;

    struct TestingScope {
        TestingScope()
        {
            constructionCount = 0;
            copyCount = 0;
            moveCount = 0;
        }
    };

    CopyMoveCounter() { constructionCount++; }
    CopyMoveCounter(const CopyMoveCounter&) { copyCount++; }
    CopyMoveCounter& operator=(const CopyMoveCounter&) { copyCount++; return *this; }
    CopyMoveCounter(CopyMoveCounter&&) { moveCount++; }
    CopyMoveCounter& operator=(CopyMoveCounter&&) { moveCount++; return *this; }
};


struct ConstructorDestructorCounter {
    static unsigned constructionCount;
    static unsigned destructionCount;

    struct TestingScope {
        TestingScope()
        {
            constructionCount = 0;
            destructionCount = 0;
        }
    };

    ConstructorDestructorCounter() { constructionCount++; }
    ~ConstructorDestructorCounter() { destructionCount++; }
};

template<typename T>
struct DeleterCounter {
    static unsigned deleterCount;

    struct TestingScope {
        TestingScope()
        {
            deleterCount = 0;
        }
    };

    void operator()(T* p) const
    {
        deleterCount++;
        delete p;
    }
};

template<class T> unsigned DeleterCounter<T>::deleterCount = 0;

#endif // Counters_h
