#pragma once
#include <unistd.h>
#include <utility>

namespace hx::tux {
    struct unique_fd {
        unique_fd() = default;

        explicit unique_fd(int fd) {
            reset(fd);
        }

        unique_fd(const unique_fd& copy) = delete;
        unique_fd(unique_fd&& move) noexcept {
            *this = std::move(move);
        }

        ~unique_fd() {
            reset();
        }

        unique_fd& operator=(const unique_fd& copy) = delete;
        unique_fd& operator=(unique_fd&& move) noexcept {
            if (this == &move) {
                return *this;
            }

            reset();

            if (move.fd_ != -1) {
                fd_ = move.fd_;
                move.fd_ = -1;
            }

            return *this;
        }

        bool operator==(int fd) {
            return fd_ == fd;
        }

        bool operator!=(int fd) {
            return !(*this == fd);
        }

        int get() { return fd_; }

        [[nodiscard]] int release() {
            if (fd_ == -1) {
                return -1;
            }

            int fd = fd_;
            fd_ = -1;

            return fd;
        }

        void reset(int new_fd = -1) {
            if (fd_ != -1) {
                close(fd_);
                fd_ = -1;
            }

            if (new_fd != -1) {
                fd_ = new_fd;
            }
        }

    private:
        int fd_{-1};
    };
}
