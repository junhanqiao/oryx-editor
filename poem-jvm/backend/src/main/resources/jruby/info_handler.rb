module Projection
  def self.project(obj, keys)
    output = {}
    keys.each do |key|
      method = "get#{key.capitalize}"
      output[key.downcase] = obj.send(method).to_s if obj.respond_to?(method)
    end
    return output
  end
end

module Handler
  class CollectionHandler < DefaultHandler

      def doGet(interaction)
        if interaction.params['from']
          from = Time.parse(interaction.params['from'])
        else 
          from = Time.parse("01-01-1970")
        end
        
        if interaction.params['to']
          to = Time.parse(interaction.params['to'])
        else
          to = Time.now+100000
        end
        
        if interaction.params['type']
          type = interaction.params['type']
        else
          type = '%'
        end
        
        models = interaction.subject.getModels(type, from, to)
        out = interaction.response.getWriter
        #TODO: Each representation and access to json inda response
        output = []
        models.each do |model|
          info = Projection.project(model, %w{Title Summary Updated Created Type})
          access = []
          Identity.instance(model.getIdent_id).getAccess.each do |right|
            access << Projection.project(right, %w{Subject Predicate Url})
          end
          output << {'info'=>info,'access'=>access}
        end
        
        out.print(ActiveSupport::JSON.encode(output))
      end
    end

  class InfoHandler < DefaultHandler
    require 'rubygems'
    require 'activesupport'
      def doGet(interaction)
        #TODO: Write Response as json
        interaction.response.setStatus(200)
        representation = interaction.object.read
        out = interaction.response.getWriter
        output = Projection.project(representation, %w{Title Summary Updated Created Type})
        out.print(ActiveSupport::JSON.encode(output))
      end
      
      def doPut(interaction)
        representation = interaction.object.read
        interaction.params.each do |key, value|
          representation.send "set#{key.capitalize}", value
        end
        representation.update
        interaction.response.setStatus(200)
      end
    end

end