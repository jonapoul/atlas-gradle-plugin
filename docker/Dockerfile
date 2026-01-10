FROM gradle:9.2.1-jdk17

RUN apt-get update
RUN apt-get install -y graphviz curl

# https://d2lang.com/tour/install/
RUN curl -fsSL https://d2lang.com/install.sh | sh -s --

# SVG to PNG converters
RUN apt-get install -y imagemagick
RUN apt-get install -y inkscape
RUN apt-get install -y librsvg2-bin
RUN apt-get install -y python3 python3-pip
RUN pip3 install --break-system-packages cairosvg

# Clear cache for reduced image size
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/*

WORKDIR /atlas
CMD ["/bin/bash"]
